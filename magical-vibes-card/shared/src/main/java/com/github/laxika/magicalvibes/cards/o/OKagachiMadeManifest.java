package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class OKagachiMadeManifest extends Card {

    public OKagachiMadeManifest() {
        addTrigger(Triggers.attacks(),
                new OpponentChoosesCardFromGraveyardToHandEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), true, true));
    }
}
