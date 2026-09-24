package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutUpToCardsFromHandOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "189")
public class TheWorldSpell extends Card {

    public TheWorldSpell() {
        CardAllOfPredicate nonSagaPermanent = new CardAllOfPredicate(List.of(
                new CardIsPermanentPredicate(),
                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.SAGA))
        ));

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(7, nonSagaPermanent));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new PutUpToCardsFromHandOntoBattlefieldEffect(nonSagaPermanent, "non-Saga permanent", 2));
    }
}
