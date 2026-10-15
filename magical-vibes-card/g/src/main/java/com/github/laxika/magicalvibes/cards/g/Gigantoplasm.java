package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "11")
public class Gigantoplasm extends Card {

    public Gigantoplasm() {
        ActivatedAbility basePowerAndToughnessAbility = new ActivatedAbility(
                false,
                "{X}",
                List.of(new SetBasePowerToughnessToAmountEffect(
                        new XValue(), new XValue(), GrantScope.SELF)),
                "{X}: This creature has base power and toughness X/X.")
                .withXValue();

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentIsCreaturePredicate(), "creature", null, null, Set.of(),
                List.of(basePowerAndToughnessAbility)));
    }
}
