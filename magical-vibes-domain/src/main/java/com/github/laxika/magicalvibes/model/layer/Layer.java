package com.github.laxika.magicalvibes.model.layer;

/**
 * The CR 613.1 layers (and CR 613.4 sublayers of layer 7) in which continuous effects are
 * applied. The enum declaration order IS the application order — the layered pass iterates
 * {@code Layer.values()} and applies every effect classified into that layer across the whole
 * battlefield before moving to the next layer. See {@code agent-docs/LAYER_SYSTEM.md}.
 */
public enum Layer {
    /** CR 613.2a — copy effects (applied to the card identity before the pass; CR 707.2). */
    L1_COPY,
    /** CR 613.2b — control-changing effects. */
    L2_CONTROL,
    /** CR 613.2c — text-changing effects (CR 612). */
    L3_TEXT,
    /** CR 613.2d — type-changing effects (card types, subtypes, supertypes). */
    L4_TYPE,
    /** CR 613.2e — color-changing effects. */
    L5_COLOR,
    /** CR 613.2f — ability-adding and ability-removing effects. */
    L6_ABILITIES,
    /** CR 613.4a — characteristic-defining P/T (the "*&#47;*" abilities). */
    L7A_CDA,
    /** CR 613.4b — effects that set power and/or toughness to specific values. */
    L7B_SET_PT,
    /** CR 613.4c — additive P/T changes, INCLUDING +1/+1 and -1/-1 counters. */
    L7C_MODIFY_PT,
    /** CR 613.4d — P/T switching effects, each applied as its own step (two switches cancel). */
    L7D_SWITCH_PT
}
