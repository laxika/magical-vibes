package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "238")
public class TheMightstoneAndWeakstone extends Card {

    public TheMightstoneAndWeakstone() {
        PermanentPredicateTargetFilter creatureFilter = TargetFilters.creature();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw two cards.", new DrawCardEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -5/-5 until end of turn.",
                        new BoostTargetCreatureEffect(-5, -5), creatureFilter)
        )));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 2, new ManaRestriction.Powerstone())),
                "{T}: Add {C}{C}. This mana can't be spent to cast nonartifact spells."
        ));
    }
}
