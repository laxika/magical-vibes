package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class OKagachiMadeManifest extends Card {

    public OKagachiMadeManifest() {
        addEffect(EffectSlot.ON_ATTACK,
                new OpponentChoosesCardFromGraveyardToHandEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)), true, true));
    }
}
