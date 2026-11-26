package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToOneCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MKM", collectorNumber = "83")
@CardRegistration(set = "MKM", collectorNumber = "399")
public class DeadlyCoverUp extends Card {

    public DeadlyCoverUp() {
        addCastingCost(new CollectEvidenceCost(6, true));
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentIsCreaturePredicate()));
        addSpellEffect(new ConditionalEffect(
                new CollectEvidenceCostPaid(),
                SequenceEffect.of(
                        new ExileUpToOneCardFromGraveyardEffect(true).from(Subjects.eachOpponent()),
                        ExileCardsWithSameNameEffect.anyNumber(Subjects.cardsThisWay(StepKind.EXILE)),
                        new DrawCardEffect(AmountThisWay.exiledFromHand(), Subjects.playersThisWay(StepKind.EXILE)))));
    }
}
