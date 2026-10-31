package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.ArrayList;
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
@CardRegistration(set = "SLD", collectorNumber = "1104")
@CardRegistration(set = "WOT", collectorNumber = "60")
public class PrismaticOmen extends Card {

    public PrismaticOmen() {
        List<CardSubtype> basicLandTypes = List.of(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        for (CardSubtype basicLandType : basicLandTypes) {
            addStaticEffect(new GrantSubtypeEffect(basicLandType, GrantScope.OWN_LANDS));
        }

        // The any-color ability stands in for the five intrinsic mana abilities, so it only applies
        // while the land still has every basic land type; a later type-setting effect (e.g. Elsewhere
        // Flask) leaves just the intrinsic ability of the type it sets.
        List<PermanentPredicate> hasEveryBasicLandType = new ArrayList<>();
        hasEveryBasicLandType.add(new PermanentIsLandPredicate());
        basicLandTypes.forEach(type -> hasEveryBasicLandType.add(new PermanentHasSubtypePredicate(type)));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                GrantScope.OWN_PERMANENTS,
                new PermanentAllOfPredicate(hasEveryBasicLandType)
        ));
    }
}
