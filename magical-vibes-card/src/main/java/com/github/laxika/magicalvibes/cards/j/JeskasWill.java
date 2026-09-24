package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "978")
@CardRegistration(set = "SLD", collectorNumber = "1744")
public class JeskasWill extends Card {

    public JeskasWill() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Add {R} for each card in target opponent's hand",
                        new AwardManaEffect(ManaColor.RED, new CardsInHand(CountScope.TARGET_PLAYER)),
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent")),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile the top three cards of your library. You may play them this turn",
                        new ExileTopCardMayPlayThisTurnEffect(3, false))
        ), new ControllerHasCommanderAsCast()));
    }
}
