package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentPoisoned;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YONE", collectorNumber = "4")
public class NornsFetchling extends Card {

    public NornsFetchling() {
        ConjureCardNamedIntoHandEffect conjurePlains =
                new ConjureCardNamedIntoHandEffect("Plains", false);
        SeekLibraryToHandEffect seekNonland = new SeekLibraryToHandEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                new ConditionalEffect(new OpponentPoisoned(3),
                        new MayEffect(seekNonland, "Seek a nonland card instead?", conjurePlains), false),
                new ConditionalEffect(new NotCondition(new OpponentPoisoned(3)), conjurePlains, false)
        ));
    }
}
