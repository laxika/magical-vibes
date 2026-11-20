package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "50")
public class FoundingTheThirdPath extends Card {

    public FoundingTheThirdPath() {
        addStaticEffect(new ReadAheadEffect(false));
        CardPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        // Chapter I: You may cast an instant or sorcery spell with mana value 1 or 2
        // from your hand without paying its mana cost.
        CardPredicate lowManaInstantOrSorcery = new CardAllOfPredicate(List.of(
                instantOrSorcery, new CardMinManaValuePredicate(1), new CardMaxManaValuePredicate(2)));
        addTrigger(Triggers.chapter(1),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(lowManaInstantOrSorcery));

        // Chapter II: Target player mills four cards.
        addTrigger(Triggers.chapter(2), new MillEffect(4, Subjects.targetPlayer()));

        // Chapter III: Exile target instant or sorcery card from your graveyard. Copy it.
        // You may cast the copy.
        addTrigger(Triggers.chapter(3),
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(instantOrSorcery),
                        CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE)).payingItsCosts()));
    }
}
