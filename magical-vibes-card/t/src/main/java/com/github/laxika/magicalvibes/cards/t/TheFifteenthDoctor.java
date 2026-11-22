package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1584")
public class TheFifteenthDoctor extends Card {

    public TheFifteenthDoctor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), millAndReturnArtifact());
        addTrigger(Triggers.attacks(), millAndReturnArtifact());
        addStaticEffect(new GrantSpellCastingAbilityToFirstMatchingSpellEachTurnEffect(
                Keyword.IMPROVISE, new CardNotPredicate(new CardTypePredicate(CardType.ARTIFACT))));
    }

    private static SequenceEffect millAndReturnArtifact() {
        return SequenceEffect.of(
                new MillEffect(3, Subjects.you()),
                ChooseCardEffect.upTo(
                        Subjects.cardsThisWay(StepKind.MILL, new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.ARTIFACT),
                                new CardMinManaValuePredicate(2),
                                new CardMaxManaValuePredicate(3)))), 1,
                        "an artifact card with mana value 2 or 3 milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
