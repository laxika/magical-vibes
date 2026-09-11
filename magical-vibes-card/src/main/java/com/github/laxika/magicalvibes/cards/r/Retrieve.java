package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToOneOfEachFilterFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "215")
public class Retrieve extends Card {

    public Retrieve() {
        CardPredicate creature = new CardTypePredicate(CardType.CREATURE);
        CardPredicate noncreaturePermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));

        addEffect(EffectSlot.SPELL, new ReturnUpToOneOfEachFilterFromGraveyardToHandEffect(
                List.of(creature, noncreaturePermanent)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
