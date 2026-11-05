package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "216")
public class MishraClaimedByGix extends Card {

    private static final String PARTNER_NAME = "Phyrexian Dragon Engine";

    public MishraClaimedByGix() {
        setBackFaceCard(new MishraLostToPhyrexia());

        PermanentCount attackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER);

        addTrigger(Triggers.youAttack(), new LoseLifeEffect(attackingCreatures, LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(attackingCreatures, GainLifeRecipient.CONTROLLER),
                new ConditionalEffect(
                new AllOf(List.of(
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsSourceCardPredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentNamedPredicate(PARTNER_NAME),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsAttackingPredicate(),
                                new PermanentOwnedBySourceControllerPredicate()))))),
                new MeldWithNamedCreatureEffect(PARTNER_NAME, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate())), true)));
    }

    @Override
    public String getBackFaceClassName() {
        return "MishraLostToPhyrexia";
    }
}
