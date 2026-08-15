package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastLifeDrainEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "KLD", collectorNumber = "183")
public class KambalConsulOfAllocation extends Card {

    public KambalConsulOfAllocation() {
        // Whenever an opponent casts a noncreature spell, that player loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastLifeDrainEffect(2, 2,
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
    }
}
