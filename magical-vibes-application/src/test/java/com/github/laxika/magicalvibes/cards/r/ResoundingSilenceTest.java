package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResoundingSilenceTest extends BaseCardTest {

    private Permanent addAttacker(Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    private void castSilence(UUID targetId) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ResoundingSilence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
    }

    @Test
    @DisplayName("Exiles the target attacking creature")
    void exilesAttackingCreature() {
        Permanent attacker = addAttacker(player1);

        castSilence(attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards)
                .anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        // Exile, not destroy — the creature never reaches a graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addAttacker(player2); // valid target elsewhere so the spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ResoundingSilence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("Cycling exiles up to two chosen attacking creatures and draws a card")
    void cyclingExilesTwoAttackersAndDraws() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent a1 = addAttacker(player2);
        Permanent a2 = addAttacker(player2);
        addMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(a1.getId(), a2.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards)
                .filteredOn(e -> e.card().getName().equals("Grizzly Bears"))
                .hasSize(2);
        // The cycling draw still happens: Resounding Silence is discarded, the library card drawn.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Resounding Silence"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling may exile fewer than two — choosing none still draws a card")
    void cyclingMayExileNone() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addAttacker(player2);
        addMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        GameData gd = harness.getGameData();
        // No attackers exiled, but the cycling draw resolves.
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling may exile exactly one of two attackers and still draws")
    void cyclingMayExileOneOfTwo() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent a1 = addAttacker(player2);
        Permanent a2 = addAttacker(player2);
        addMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(a1.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards)
                .filteredOn(e -> e.card().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(a2.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cycling with no attacking creatures still draws a card")
    void cyclingWithNoAttackersStillDraws() {
        harness.setHand(player1, List.of(new ResoundingSilence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
