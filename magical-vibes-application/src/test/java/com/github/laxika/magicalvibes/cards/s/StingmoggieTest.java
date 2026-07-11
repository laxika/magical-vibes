package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StingmoggieTest extends BaseCardTest {

    // ===== ETB: enters with two +1/+1 counters =====

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters (0/0 becomes 2/2)")
    void entersWithTwoCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Stingmoggie()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (counters applied as it enters)

        Permanent moggie = findMoggie(player1);

        assertThat(moggie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(moggie.getEffectivePower()).isEqualTo(2);
        assertThat(moggie.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Ability destroys target artifact and removes a +1/+1 counter as cost")
    void abilityDestroysArtifact() {
        Permanent moggie = addReadyMoggie(player1);
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        int idx = indexOf(player1, moggie);
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.activateAbility(player1, idx, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        // Started with 2 counters, removed 1 as cost
        assertThat(moggie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability destroys target land")
    void abilityDestroysLand() {
        Permanent moggie = addReadyMoggie(player1);
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        int idx = indexOf(player1, moggie);
        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.activateAbility(player1, idx, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature (neither artifact nor land)")
    void cannotTargetCreature() {
        Permanent moggie = addReadyMoggie(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        int idx = indexOf(player1, moggie);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot activate without counters =====

    @Test
    @DisplayName("Cannot activate ability when no +1/+1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent moggie = addReadyMoggie(player1);
        moggie.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.addToBattlefield(player2, new LeoninScimitar());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        int idx = indexOf(player1, moggie);
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyMoggie(Player player) {
        Permanent perm = new Permanent(new Stingmoggie());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private Permanent findMoggie(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Stingmoggie"))
                .findFirst().orElseThrow();
    }
}
