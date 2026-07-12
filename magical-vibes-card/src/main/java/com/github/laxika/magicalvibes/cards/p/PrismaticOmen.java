package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

/**
 * Prismatic Omen — "Lands you control are every basic land type in addition to their other types."
 *
 * <p>The type change is modeled as five additive {@link GrantSubtypeEffect}s scoped to the lands you
 * control (layer 4, in addition to their other types per rule 305.6 — the printed types remain). The
 * resulting five basic land types would each carry their intrinsic mana ability, so the practical
 * effect is that any land you control can tap for one mana of any color; that is granted directly via
 * the Joiner Adept-style "{T}: Add one mana of any color" ability rather than five separate abilities.
 */
@CardRegistration(set = "SHM", collectorNumber = "126")
public class PrismaticOmen extends Card {

    public PrismaticOmen() {
        for (CardSubtype basicLandType : List.of(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST)) {
            addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(basicLandType, GrantScope.OWN_LANDS));
        }

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new AwardAnyColorManaEffect()),
                        "{T}: Add one mana of any color."
                ),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsLandPredicate()
        ));
    }
}
