package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithPermanentsPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "1677")
@CardRegistration(set = "SLD", collectorNumber = "1877")
public class LightPawsEmperorsVoice extends Card {

    public LightPawsEmperorsVoice() {
        // "An Aura card with mana value less than or equal to that Aura and with a different name than each Aura you
        // control": a found Aura that can't be attached to Light-Paws stays in the library (its ruling).
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.ENCHANTMENT,
                                new PermanentHasSubtypePredicate(CardSubtype.AURA))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new WasCast(true)),
                new MayEffect(SequenceEffect.of(
                        new SearchLibraryEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.AURA),
                                        new CardNotPredicate(new CardSharesNameWithPermanentsPredicate(
                                                Subjects.controlledBy(Subjects.you(),
                                                        new PermanentHasSubtypePredicate(CardSubtype.AURA)))))),
                                LibrarySearchDestination.KEEP_IN_LIBRARY,
                                new ManaValueBound(new TriggeringPermanentManaValue(), false, 0), false),
                        new PutCardsOntoBattlefieldEffect(Subjects.foundThisWay()).attachedTo(Subjects.source()),
                        new ShuffleLibraryEffect(false)),
                        "Search your library for an Aura card to put onto the battlefield attached to Light-Paws?"));
    }
}
