package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "FIC", collectorNumber = "29")
@CardRegistration(set = "FIC", collectorNumber = "109")
public class SummonersSending extends Card {

    public SummonersSending() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new MayEffect(
                new ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect(
                        CreateTokenEffect.whiteSpirit(1), 4),
                "Exile target creature card from a graveyard?"));
    }
}
