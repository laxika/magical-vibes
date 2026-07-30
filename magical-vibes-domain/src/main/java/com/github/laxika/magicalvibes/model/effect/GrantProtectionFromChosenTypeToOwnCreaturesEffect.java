package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * STATIC: creatures you control with {@code recipientSubtype} have protection from creatures of the
 * creature type chosen for the source permanent ({@code Permanent.getChosenSubtype()}). Pair with
 * {@link ChooseSubtypeOnEnterEffect} in {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ENTER_BATTLEFIELD}
 * for the choice (Riders of Gavony).
 *
 * <p>The grant covers the source permanent itself when it also has {@code recipientSubtype}, and is
 * realised as a granted {@link ProtectionFromSubtypesEffect} with {@code creatureSourcesOnly} set —
 * the protection stops creature sources of the chosen type only, not a noncreature source that
 * happens to carry the type.
 *
 * @param recipientSubtype the subtype a controlled creature must have to receive the protection
 */
public record GrantProtectionFromChosenTypeToOwnCreaturesEffect(CardSubtype recipientSubtype)
        implements CardEffect {
}
