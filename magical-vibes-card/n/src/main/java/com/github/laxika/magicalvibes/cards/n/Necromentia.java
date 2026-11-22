package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "M21", collectorNumber = "116")
@CardRegistration(set = "M21", collectorNumber = "359")
public class Necromentia extends Card {

    public Necromentia() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(ChooseCardNameAtResolutionEffect.otherThanABasicLand())
                .addSpellEffect(ExileCardsWithSameNameEffect.anyNumberWithChosenName(Subjects.targetPlayer()))
                .addSpellEffect(CreateTokenEffect.blackZombie(1).withAmount(AmountThisWay.exiledFromHand())
                        .forController(Subjects.targetPlayer()));
    }
}
