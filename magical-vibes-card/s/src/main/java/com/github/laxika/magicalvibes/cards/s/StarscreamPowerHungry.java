package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOT", collectorNumber = "5")
@CardRegistration(set = "BOT", collectorNumber = "20")
public class StarscreamPowerHungry extends Card {

    public StarscreamPowerHungry() {
        setBackFaceCard(new StarscreamSeekerLeader());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{B}"));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.youDrawCard(), new ConditionalEffect(
                new ControllerIsMonarch(),
                new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER)));

        addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE, new TransformSelfEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "StarscreamSeekerLeader";
    }
}
