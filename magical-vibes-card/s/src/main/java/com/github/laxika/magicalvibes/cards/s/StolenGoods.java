package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AVR", collectorNumber = "78")
@CardRegistration(set = "C15", collectorNumber = "107")
@CardRegistration(set = "OTC", collectorNumber = "115")
public class StolenGoods extends Card {

    public StolenGoods() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Must target an opponent"
        )).addSpellEffect(ExileCardsFromTopUntilEffect.untilNonland(Subjects.targetPlayer()));
        addSpellEffect(AllowPlayCardsFromExileEffect.thisTurn(
                Subjects.cardsThisWay(StepKind.EXILE, ExileCardsFromTopUntilEffect.NONLAND)).withoutPayingManaCost());
    }
}
