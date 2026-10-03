package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientHellkite.class, LlanowarElves.class, RuneclawBear.class})
class AncientHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature defending player controls while attacking — kills 1/1")
    void dealsOneDamageWhileAttacking() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new LlanowarElves());

        setUpAttacking(hellkite);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Llanowar Elves is 1/1, 1 damage kills it
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Can activate multiple times per turn — no per-turn activation limit")
    void canActivateMultipleTimes() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player2, new RuneclawBear());

        setUpAttacking(hellkite);

        // Stack two activations targeting different creatures
        UUID target1 = harness.getPermanentId(player2, "Llanowar Elves");
        UUID target2 = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, target1);
        harness.activateAbility(player1, 0, null, target2);
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate ability when not attacking")
    void cannotActivateWhenNotAttacking() {
        addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player2, new RuneclawBear());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    @Test
    @DisplayName("Cannot target own creatures")
    void cannotTargetOwnCreatures() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        addCreatureReady(player1, new RuneclawBear());

        setUpAttacking(hellkite);

        UUID ownTargetId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownTargetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations deal cumulative damage to the same creature")
    void repeatedDamageKillsCreature() {
        addCreatureReady(player1, new AncientHellkite());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(target.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("An activated ability still resolves after Hellkite stops attacking")
    void resolvesAfterSourceStopsAttacking() {
        Permanent hellkite = addCreatureReady(player1, new AncientHellkite());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        hellkite.setAttacking(false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target the defending player directly")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new AncientHellkite());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void setUpAttacking(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        attacker.setAttacking(true);
    }
}
