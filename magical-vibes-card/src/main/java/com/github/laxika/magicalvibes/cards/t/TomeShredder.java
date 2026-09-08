package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "117")
public class TomeShredder extends Card {

    public TomeShredder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ExileCardFromGraveyardCost(CardType.INSTANT, CardType.SORCERY),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "{T}, Exile an instant or sorcery card from your graveyard: Put a +1/+1 counter on this creature."
        ));
    }
}
