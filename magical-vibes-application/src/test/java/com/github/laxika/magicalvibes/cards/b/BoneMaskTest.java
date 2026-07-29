package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoneMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        addReadyBoneMask(player1);
        addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the chosen source's next damage and exiles that many cards from the library")
    void preventsDamageAndExilesCards() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GoblinPiker(), new GoblinPiker(), new GoblinPiker()));
        addReadyBoneMask(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        // Goblin Piker is 2/1 — two cards exiled, one left in the library
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Exiles only as many cards as the library holds")
    void exilesNoMoreThanTheLibraryHolds() {
        harness.setLibrary(player1, List.of(new GoblinPiker()));
        addReadyBoneMask(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(1);
    }

    @Test
    @DisplayName("A different source still deals damage and exiles nothing")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GoblinPiker(), new GoblinPiker()));
        addReadyBoneMask(player1);
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyBoneMask(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyBoneMask(Player player) {
        return addReady(player, new BoneMask());
    }

    private Permanent addReadyGoblin(Player player) {
        return addReady(player, new GoblinPiker());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
