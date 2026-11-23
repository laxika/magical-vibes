package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetGroupMarkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "228")
public class IrohTeaMaster extends Card {

    public IrohTeaMaster() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), foodToken());

        TargetGroupMarkerEffect opponent = new TargetGroupMarkerEffect();
        MayEffect giveAway = new MayEffect(
                new GainControlOfTargetEffect(Duration.PERMANENT).gainedBy(Subjects.targetPlayer(0)),
                "Have target opponent gain control of target permanent you control?");
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(opponent);
        target(TargetFilters.permanentYouControl()).bindEffect(giveAway);
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU), opponent, giveAway,
                new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL),
                        new QueueReflexiveAbilityEffect(allyToken())));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )
        ));
    }

    private static CreateXTokenWithXCountersEffect allyToken() {
        return new CreateXTokenWithXCountersEffect(
                new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY),
                        Set.of(), Set.of()),
                new PermanentCount(new PermanentOwnedBySourceControllerPredicate(), CountScope.OPPONENTS),
                CounterType.PLUS_ONE_PLUS_ONE
        );
    }
}
