package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "29")
@CardRegistration(set = "FIC", collectorNumber = "109")
public class SummonersSending extends Card {

    public SummonersSending() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new MayEffect(
                new ExileTargetCreatureCardFromGraveyardCreateTokenWithManaValueCounterEffect(
                        CreateTokenEffect.whiteSpirit(1), 4),
                "Exile target creature card from a graveyard?"));
    }
}
