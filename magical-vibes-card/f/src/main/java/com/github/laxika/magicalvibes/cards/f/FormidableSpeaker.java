package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "176")
@CardRegistration(set = "ECL", collectorNumber = "366")
public class FormidableSpeaker extends Card {

    public FormidableSpeaker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new DiscardEffect(1, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new SearchLibraryEffect(new CardTypePredicate(CardType.CREATURE)))),
                "Discard a card to search your library for a creature card?"));

        var anotherPermanent = new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new UntapPermanentsEffect(Subjects.target(), anotherPermanent)),
                "{1}, {T}: Untap another target permanent.",
                new PermanentPredicateTargetFilter(anotherPermanent, "Target must be another permanent")));
    }
}
