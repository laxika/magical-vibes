package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveTimeCounterWhenOpponentCastsSpellEffect;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "56")
public class DeepSeaKraken extends Card {

    public DeepSeaKraken() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new RemoveTimeCounterWhenOpponentCastsSpellEffect());
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(),
                "Suspend 9—{2}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(9));
    }
}
