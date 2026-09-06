package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "94")
public class ArcBlade extends Card {

    public ArcBlade() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect(3));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(),
                "Suspend 3—{2}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
