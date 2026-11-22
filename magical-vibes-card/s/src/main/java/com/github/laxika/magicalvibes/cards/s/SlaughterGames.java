package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsWithSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "RTR", collectorNumber = "197")
@CardRegistration(set = "EA1", collectorNumber = "17")
public class SlaughterGames extends Card {

    public SlaughterGames() {
        addStaticEffect(new CantBeCounteredEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND))
                .addSpellEffect(ExileCardsWithSameNameEffect.anyNumberWithChosenName(Subjects.targetPlayer()));
    }
}
