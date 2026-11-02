package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "45")
public class NecraSanctuary extends Card {

    public NecraSanctuary() {
        var controlsGreen = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.GREEN)));
        var controlsWhite = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.WHITE)));
        var controlsGreenAndWhite = new AllOf(List.of(controlsGreen, controlsWhite));

        var targetPlayer = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        ));
        targetPlayer.addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new AnyOf(List.of(controlsGreen, controlsWhite)),
                new LoseLifeEffect(new FixedIfCondition(controlsGreenAndWhite, 3, 1), LoseLifeRecipient.TARGET_PLAYER)));
    }
}
