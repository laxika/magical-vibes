package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "XLN", collectorNumber = "94")
public class BoneyardParley extends Card {

    public BoneyardParley() {
        addEffect(EffectSlot.SPELL, new ExileTargetGraveyardCardsAndSeparateIntoPilesEffect(
                new CardTypePredicate(CardType.CREATURE), 5
        ));
    }
}
