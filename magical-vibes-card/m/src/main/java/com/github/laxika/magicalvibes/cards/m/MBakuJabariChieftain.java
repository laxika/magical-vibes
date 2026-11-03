package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttackedTargetIsMonarch;
import com.github.laxika.magicalvibes.model.condition.NoMonarch;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerBecomesMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "67")
@CardRegistration(set = "MSC", collectorNumber = "382")
public class MBakuJabariChieftain extends Card {

    public MBakuJabariChieftain() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new NoMonarch()),
                new TargetPlayerBecomesMonarchEffect());

        addTrigger(Triggers.creatureAttacks(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsAttackingOpponentOfSourceControllerPredicate(),
                        new ConditionalEffect(
                                new AttackedTargetIsMonarch(),
                                SequenceEffect.of(
                                        new BoostTargetCreatureEffect(1, 1),
                                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)),
                                true)));
    }
}
