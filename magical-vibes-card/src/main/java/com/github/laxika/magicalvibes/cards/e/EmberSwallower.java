package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "120")
public class EmberSwallower extends Card {

    public EmberSwallower() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}{R}",
                List.of(new MonstrosityEffect(3)),
                "{5}{R}{R}: Monstrosity 3."
        ));

        addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, new SacrificePermanentsEffect(
                3, new PermanentIsLandPredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
