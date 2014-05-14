#define FF_S3TC_DXT1    0x31545844                                                     
#define FF_S3TC_DXT3    0x33545844

static void ff_decode_dxt1(const uint8_t *src, uint8_t *dst,
                     const uint32_t w, const uint32_t h,
                     const uint32_t stride);
                     
static void ff_decode_dxt3(const uint8_t *src, uint8_t *dst,
                     const uint32_t w, const uint32_t h,
                     const uint32_t stride);
                                                                                                                                                    