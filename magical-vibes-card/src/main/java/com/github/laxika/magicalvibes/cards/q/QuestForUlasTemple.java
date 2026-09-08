package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingAndPutCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "35")
public class QuestForUlasTemple extends Card {

    public QuestForUlasTemple() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayRevealMatchingAndPutCounterEffect(
                        new CardTypePredicate(CardType.CREATURE), CounterType.QUEST));

        CardAllOfPredicate seaCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.KRAKEN),
                        new CardSubtypePredicate(CardSubtype.LEVIATHAN),
                        new CardSubtypePredicate(CardSubtype.OCTOPUS),
                        new CardSubtypePredicate(CardSubtype.SERPENT)
                ))
        ));
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.QUEST),
                new MayEffect(
                        new PutCardToBattlefieldEffect(
                                seaCreature,
                                "Kraken, Leviathan, Octopus, or Serpent creature"),
                        "Put a Kraken, Leviathan, Octopus, or Serpent creature card from your hand "
                                + "onto the battlefield?")));
    }
}
