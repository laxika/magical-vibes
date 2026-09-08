package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "201")
public class Hypergenesis extends Card {

    public Hypergenesis() {
        addEffect(EffectSlot.SPELL, EachPlayerMayPutCardFromHandToBattlefieldEffect.hypergenesis());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{G}",
                List.of(),
                "Suspend 3—{1}{G}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
