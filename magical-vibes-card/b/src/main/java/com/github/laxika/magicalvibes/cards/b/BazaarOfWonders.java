package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNameFoundElsewhereEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MIR", collectorNumber = "55")
public class BazaarOfWonders extends Card {

    public BazaarOfWonders() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addTrigger(Triggers.playerCastsSpell(), new CounterSpellIfNameFoundElsewhereEffect());
    }
}
