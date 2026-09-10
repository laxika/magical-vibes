package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "DSK", collectorNumber = "187")
public class KonaRescueBeastie extends Card {

    public KonaRescueBeastie() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED, new SurvivalTriggerEffect(
                new ConditionalEffect(
                        new SourceIsTapped(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent"),
                                "Put a permanent card from your hand onto the battlefield?"))));
    }
}
