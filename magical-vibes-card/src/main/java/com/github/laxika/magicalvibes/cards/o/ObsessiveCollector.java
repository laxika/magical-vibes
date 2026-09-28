package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;

@CardRegistration(set = "YMID", collectorNumber = "19")
public class ObsessiveCollector extends Card {

    public ObsessiveCollector() {
        // Ward {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        // Whenever Obsessive Collector deals combat damage to a player, seek a card with mana
        // value equal to the number of cards in your hand.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SeekCardsToHandEffect(
                new Fixed(1), null,
                new ManaValueBound(new CardsInHand(CountScope.CONTROLLER), true, 0)));
    }
}
