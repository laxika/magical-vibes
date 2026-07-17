package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NoOtherPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "140")
public class ThopterAssembly extends Card {

    public ThopterAssembly() {
        // Single atomic triggered ability (one stack entry): the intervening-if condition gates the
        // whole sequence, and the two steps resolve in oracle order with no contingency between them.
        // If Thopter Assembly already left the battlefield the bounce silently no-ops, but the tokens
        // are still created (verified ruling).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new NoOtherPermanent(new PermanentHasSubtypePredicate(CardSubtype.THOPTER)),
                SequenceEffect.of(
                        ReturnToHandEffect.self(),
                        new CreateTokenEffect(5, "Thopter", 1, 1,
                                null, List.of(CardSubtype.THOPTER),
                                Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT)))));
    }
}
