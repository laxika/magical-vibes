package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rocketeer Boostbuggy")
class RocketeerBoostbuggyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when it attacks")
    void createsTreasureWhenItAttacks() {
        addReady(new RocketeerBoostbuggy());
        addReady(new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
    }

    @Test
    @DisplayName("Exhaust makes it a permanent artifact creature and puts a counter on it")
    void exhaustMakesItPermanentArtifactCreatureAndPutsCounterOnIt() {
        Permanent buggy = addReady(new RocketeerBoostbuggy());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, buggy)).isTrue();
        assertThat(gqs.isArtifact(buggy)).isTrue();
        assertThat(buggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, buggy)).isTrue();
    }

    @Test
    @DisplayName("Exhaust can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        addReady(new RocketeerBoostbuggy());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
