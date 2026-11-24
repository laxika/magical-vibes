package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChosenCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "100")
@CardRegistration(set = "DBL", collectorNumber = "367")
public class CemeteryDesecrator extends Card {

    public CemeteryDesecrator() {
        // When this creature enters or dies, exile another card from a graveyard. When you do, choose one —
        // • Remove X counters from target permanent, where X is the mana value of the exiled card.
        // • Target creature an opponent controls gets -X/-X until end of turn, where X is the mana value of the
        //   exiled card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileAnotherCardThenChooseOne());
        addTrigger(Triggers.dies(ObjectRelation.THIS), exileAnotherCardThenChooseOne());
    }

    private static CardEffect exileAnotherCardThenChooseOne() {
        AmountThisWay x = AmountThisWay.exiledManaValue();
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove X counters from target permanent",
                        new RemoveChosenCountersFromTargetPermanentEffect(x, true),
                        TargetFilters.permanent()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature an opponent controls gets -X/-X until end of turn",
                        new BoostTargetCreatureEffect(new Scaled(x, -1), new Scaled(x, -1),
                                TargetFilters.creatureAnOpponentControls().predicate()),
                        TargetFilters.creatureAnOpponentControls())));
        return SequenceEffect.of(
                ChooseCardEffect.oneOf(Subjects.cardsInGraveyards(Subjects.eachPlayer(),
                        new CardNotPredicate(new CardIsSelfPredicate())), "another card from a graveyard to exile"),
                ExileGraveyardCardsEffect.cards(Subjects.cardsThisWay(StepKind.CHOOSE)),
                new ConditionalEffect(HappenedThisWay.exiled(), new QueueReflexiveAbilityEffect(modes)));
    }
}
