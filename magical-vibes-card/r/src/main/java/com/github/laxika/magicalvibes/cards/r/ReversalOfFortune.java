package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "77")
public class ReversalOfFortune extends Card {

    public ReversalOfFortune() {
        // Target opponent reveals their hand. You may copy an instant or sorcery card in it. If you do, you may cast
        // the copy without paying its mana cost.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new ChooseCardsFromTargetHandEffect(new Fixed(1), List.of(),
                        List.of(CardType.INSTANT, CardType.SORCERY), HandChoiceDestination.KEEP_IN_HAND, false, null,
                        0, true, false, true));
        addSpellEffect(CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
