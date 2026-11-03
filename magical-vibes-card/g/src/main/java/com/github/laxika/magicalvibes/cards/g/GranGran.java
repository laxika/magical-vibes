package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "54")
public class GranGran extends Card {

    public GranGran() {
        addTrigger(Triggers.becomesTapped(),
                SequenceEffect.of(new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)));

        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(3, new CardSubtypePredicate(CardSubtype.LESSON)),
                new ReduceCastCostForMatchingSpellsEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        1, CostModificationScope.SELF)));
    }
}
