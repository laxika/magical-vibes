package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.RollD8Effect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFC", collectorNumber = "58")
public class ClayGolem extends Card {

    public ClayGolem() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new RollD8Effect(new MonstrosityEffect(new XValue()), true)),
                "{6}, Roll a d8: Monstrosity X, where X is the result."
        ));

        target(TargetFilters.permanent()).addEffect(
                EffectSlot.ON_SELF_BECOMES_MONSTROUS,
                new DestroyTargetPermanentEffect(false));
    }
}
