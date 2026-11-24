package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfControllersLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "35")
public class QuestForUlasTemple extends Card {

    public QuestForUlasTemple() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(SequenceEffect.of(
                new LookAtTopCardOfControllersLibraryEffect(),
                new ConditionalEffect(new TopCardOfLibraryType(CardType.CREATURE), new MayEffect(SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.you()),
                        new PutCountersOnSelfEffect(CounterType.QUEST)),
                        "Reveal the creature card and put a quest counter on Quest for Ula's Temple?"))),
                "Look at the top card of your library?"));

        CardAllOfPredicate seaCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.KRAKEN),
                        new CardSubtypePredicate(CardSubtype.LEVIATHAN),
                        new CardSubtypePredicate(CardSubtype.OCTOPUS),
                        new CardSubtypePredicate(CardSubtype.SERPENT)
                ))
        ));
        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(3, CounterType.QUEST)),
                new MayEffect(
                        new PutCardToBattlefieldEffect(
                                seaCreature,
                                "Kraken, Leviathan, Octopus, or Serpent creature"),
                        "Put a Kraken, Leviathan, Octopus, or Serpent creature card from your hand "
                                + "onto the battlefield?"));
    }
}
