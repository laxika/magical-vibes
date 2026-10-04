package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "292")
@CardRegistration(set = "C19", collectorNumber = "273")
@CardRegistration(set = "BFZ", collectorNumber = "245")
public class ShrineOfTheForsakenGods extends Card {

    public ShrineOfTheForsakenGods() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {T}: Add {C}{C}. Spend this mana only to cast colorless spells. Activate only if you control seven or more lands.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 2, new ManaRestriction.ColorlessSpells())),
                "{T}: Add {C}{C}. Spend this mana only to cast colorless spells. Activate only if you control seven or more lands."
        ).withRequiredControlledPermanents(new PermanentIsLandPredicate(), 7, "lands"));
    }
}
