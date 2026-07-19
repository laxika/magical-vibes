package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sludge Strider")
class SludgeStriderTest extends BaseCardTest {

    // ===== Another artifact you control ENTERS =====

    @Test
    @DisplayName("Another artifact entering lets you pay {1} to drain target player")
    void artifactEnterDrains() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Spellbook()));

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // Spellbook resolves and enters, trigger onto stack
        harness.passBothPriorities(); // trigger resolves → "may pay {1}" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    // ===== Another artifact you control LEAVES =====

    @Test
    @DisplayName("Another artifact leaving lets you pay {1} to drain target player")
    void artifactLeaveDrains() {
        harness.addToBattlefield(player1, new SludgeStrider());
        Permanent book = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        // Opponent destroys your artifact; it was under your control when it left, so the trigger fires.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0, book.getId());
        harness.passBothPriorities(); // Shatter resolves, Spellbook destroyed, trigger enqueued
        harness.passBothPriorities(); // trigger resolves → "may pay {1}" prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    // ===== Declining the optional payment =====

    @Test
    @DisplayName("Declining the trigger drains no one and spends no mana")
    void declineDrainsNoOne() {
        harness.addToBattlefield(player1, new SludgeStrider());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Control scoping: an opponent's artifact does not trigger =====

    @Test
    @DisplayName("An artifact an opponent controls entering does not trigger Sludge Strider")
    void opponentArtifactEnterDoesNotTrigger() {
        harness.addToBattlefield(player1, new SludgeStrider());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }
}
