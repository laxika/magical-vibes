package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OnduWarCleric;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireEnvoy.class, GrizzlyBears.class, OnduWarCleric.class})
class VampireEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("Vampire Envoy gains 1 life when it becomes tapped")
    void gainsLifeWhenItBecomesTapped() {
        Permanent envoy = addCreatureReady(player1, new VampireEnvoy());
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(11);
        assertThat(envoy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vampire Envoy does not trigger when another creature becomes tapped")
    void doesNotTriggerWhenAnotherCreatureBecomesTapped() {
        harness.addToBattlefield(player1, new VampireEnvoy());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void gainsLifeWhenSummoningSickEnvoyPaysCohortCost() {
        addCreatureReady(player1, new OnduWarCleric());
        Permanent envoy = harness.addToBattlefieldAndReturn(player1, new VampireEnvoy());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(envoy.isTapped()).isTrue();
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachAttackingEnvoyTriggersOnlyForItself() {
        addCreatureReady(player1, new VampireEnvoy());
        addCreatureReady(player1, new VampireEnvoy());
        harness.setLife(player1, 10);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    void opponentControlledEnvoyGainsLifeForItsController() {
        harness.addToBattlefield(player1, new VampireEnvoy());
        addCreatureReady(player2, new VampireEnvoy());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.assertLife(player2, 11);
    }
}
