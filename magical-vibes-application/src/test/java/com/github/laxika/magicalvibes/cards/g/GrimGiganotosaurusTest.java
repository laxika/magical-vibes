package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.s.SerratedArrows;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimGiganotosaurus.class, CrawWurm.class, SerratedArrows.class})
class GrimGiganotosaurusTest extends BaseCardTest {

    @Test
    @DisplayName("High-power creatures opponents control reduce the monstrosity activation cost")
    void highPowerOpposingCreaturesReduceActivationCost() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        addCreatureReady(player2, new CrawWurm());
        addCreatureReady(player2, new CrawWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Becoming monstrous destroys all other artifacts and creatures")
    void becomingMonstrousDestroysOtherArtifactsAndCreatures() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        Permanent otherCreature = addCreatureReady(player1, new CrawWurm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SerratedArrows());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dinosaur)
                .doesNotContain(otherCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @CardUsed({GrimGiganotosaurus.class})
    @DisplayName("Monstrosity can be activated again but has no effect once monstrous")
    void canActivateAgainAfterBecomingMonstrous() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        Permanent survivor = addCreatureReady(player2, new GrimGiganotosaurus());
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();

        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
    }

    @Test
    @CardUsed({GrimGiganotosaurus.class})
    @DisplayName("Only opposing creatures with current power at least four reduce the cost")
    void reductionUsesCurrentPowerAndOpposingController() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        addCreatureReady(player1, new GrimGiganotosaurus());
        Permanent qualifying = addCreatureReady(player2, new GrimGiganotosaurus());
        qualifying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 6);
        Permanent tooSmall = addCreatureReady(player2, new GrimGiganotosaurus());
        tooSmall.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 7);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        resolveAllTriggers();
        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    @CardUsed({GrimGiganotosaurus.class})
    @DisplayName("Cost reduction cannot reduce the colored mana requirement")
    void reductionStopsAtColoredMana() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        for (int i = 0; i < 11; i++) {
            addCreatureReady(player2, new GrimGiganotosaurus());
        }
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();
        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed({GrimGiganotosaurus.class})
    @DisplayName("Multiple pending monstrosity activations add counters and trigger destruction only once")
    void multiplePendingActivationsBecomeMonstrousOnlyOnce() {
        Permanent dinosaur = addCreatureReady(player1, new GrimGiganotosaurus());
        harness.addMana(player1, ManaColor.COLORLESS, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(dinosaur.isMonstrous()).isTrue();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player2, new GrimGiganotosaurus());
        resolveAllTriggers();

        assertThat(dinosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(laterCreature);
    }
}
