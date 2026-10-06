package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResoundingSilence.class, CylianElf.class})
class ResoundingSilenceTest extends BaseCardTest {

    private Permanent addAttacker(Player owner) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, new CylianElf());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void castSilence(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new ResoundingSilence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castInstant(player2, 0, targetId);
    }

    @Test
    @DisplayName("Exiles the target attacking creature")
    void exilesAttackingCreature() {
        Permanent attacker = addAttacker(player1);

        castSilence(attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Cylian Elf");
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getName().equals("Cylian Elf"));
        // Exile, not destroy — the creature never reaches a graveyard.
        harness.assertNotInGraveyard(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2); // valid target elsewhere so the spell is playable
        harness.addToBattlefield(player1, new CylianElf());
        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new ResoundingSilence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cycling exiles up to two chosen attacking creatures and draws a card")
    void cyclingExilesTwoAttackersAndDraws() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        Permanent a1 = addAttacker(player2);
        Permanent a2 = addAttacker(player2);
        addMana(player1);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(a1.getId(), a2.getId()));
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        assertThat(gd.exiledCards)
                .filteredOn(e -> e.card().getName().equals("Cylian Elf"))
                .hasSize(2);
        // The cycling draw still happens: Resounding Silence is discarded, the library card drawn.
        harness.assertInGraveyard(player1, "Resounding Silence");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling may exile fewer than two — choosing none still draws a card")
    void cyclingMayExileNone() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        addAttacker(player2);
        addMana(player1);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No attackers exiled, but the cycling draw resolves.
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Cylian Elf"));
        harness.assertOnBattlefield(player2, "Cylian Elf");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling may exile exactly one of two attackers and still draws")
    void cyclingMayExileOneOfTwo() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        Permanent a1 = addAttacker(player2);
        Permanent a2 = addAttacker(player2);
        addMana(player1);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(a1.getId()));
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards)
                .filteredOn(e -> e.card().getName().equals("Cylian Elf"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(a2.getId()));
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling with no attacking creatures still draws a card")
    void cyclingWithNoAttackersStillDraws() {
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addToBattlefield(player2, new CylianElf());
        addMana(player1);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of());
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Cylian Elf"));
        harness.assertOnBattlefield(player2, "Cylian Elf");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("A creature that stops attacking before the spell resolves is not exiled")
    void spellRechecksAttackingStatus() {
        Permanent attacker = addAttacker(player1);
        castSilence(attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cylian Elf");
        harness.assertInGraveyard(player2, "Resounding Silence");
    }

    @Test
    @DisplayName("Cycling exile resolves separately before the draw")
    void cyclingTriggerResolvesBeforeDraw() {
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        addMana(player1);
        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of());
        harness.assertInGraveyard(player1, "Resounding Silence");
        harness.passBothPriorities();
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
