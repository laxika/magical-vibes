package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuralRecruit.class, GrizzlyBears.class})
class RuralRecruitTest extends BaseCardTest {

    @Test
    void entersAndCreatesABoarToken() {
        Permanent recruit = castRuralRecruit();

        harness.passBothPriorities();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent boar = findPermanent(player1, "Boar");
        assertThat(boar.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, boar)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boar)).isEqualTo(1);
    }

    @Test
    void trainingPutsACounterOnRuralRecruitWhenAttackingWithAGreaterPowerCreature() {
        Permanent recruit = addCreatureReady(player1, new RuralRecruit());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castRuralRecruit() {
        harness.setHand(player1, List.of(new RuralRecruit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Rural Recruit");
    }
}
