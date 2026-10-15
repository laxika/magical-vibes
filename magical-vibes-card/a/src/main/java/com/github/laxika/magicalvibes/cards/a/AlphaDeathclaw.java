package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "91")
@CardRegistration(set = "PIP", collectorNumber = "619")
@CardRegistration(set = "PIP", collectorNumber = "336")
@CardRegistration(set = "PIP", collectorNumber = "864")
public class AlphaDeathclaw extends Card {

    public AlphaDeathclaw() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}{G}",
                List.of(new MonstrosityEffect(4)),
                "{5}{B}{G}: Monstrosity 4."
        ));

        DestroyTargetPermanentEffect destroyTargetPermanent = new DestroyTargetPermanentEffect();
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, destroyTargetPermanent)
                .addEffect(EffectSlot.ON_SELF_BECOMES_MONSTROUS, destroyTargetPermanent);
    }
}
