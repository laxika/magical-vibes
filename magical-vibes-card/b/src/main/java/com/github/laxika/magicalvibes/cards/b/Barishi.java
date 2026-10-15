package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "WTH", collectorNumber = "119")
public class Barishi extends Card {

    public Barishi() {
        // When this creature dies, exile it, then shuffle all creature cards from your graveyard
        // into your library. One triggered ability, so both halves are a single SequenceEffect —
        // and the self-exile has to precede the shuffle, otherwise Barishi would shuffle itself in.
        addEffect(EffectSlot.ON_DEATH, SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                new ShuffleGraveyardIntoLibraryEffect(false, new CardTypePredicate(CardType.CREATURE))));
    }
}
