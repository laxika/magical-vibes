package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "216")
public class MishraClaimedByGix extends Card {

    private static final String PARTNER_NAME = "Phyrexian Dragon Engine";

    public MishraClaimedByGix() {
        setBackFaceCard(new MishraLostToPhyrexia());

        PermanentCount attackingCreatures = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new LoseLifeEffect(attackingCreatures, LoseLifeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new GainLifeEffect(attackingCreatures, GainLifeRecipient.CONTROLLER));

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
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
                new MeldWithNamedCreatureEffect(PARTNER_NAME, true)));
    }

    @Override
    public String getBackFaceClassName() {
        return "MishraLostToPhyrexia";
    }
}
