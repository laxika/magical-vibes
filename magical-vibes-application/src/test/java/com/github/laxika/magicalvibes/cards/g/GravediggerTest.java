package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GravediggerTest extends BaseCardTest {

    /**
     * Casts Gravedigger and resolves it onto the battlefield, then accepts the may ability
     * so the ETB inner effect resolves inline.
     */
    private void castAndAcceptMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → inner resolves inline
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Gravedigger puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gravedigger");
    }

    @Test
    @DisplayName("Cannot cast Gravedigger without enough mana")
    void cannotCastWithoutMana() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving puts Gravedigger on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravedigger");
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving Gravedigger triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may ability resolves ETB inline and prompts graveyard choice")
    void acceptingMayResolvesEtbInline() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        // Inner effect resolves inline — graveyard choice should be prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
    }

    @Test
    @DisplayName("Declining may ability does not put anything on the stack")
    void decliningMaySkipsAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Gravedigger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        // Gravedigger still on battlefield
        harness.assertOnBattlefield(player1, "Gravedigger");
        // Grizzly Bears still in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Graveyard return to hand =====

    @Test
    @DisplayName("Returns creature from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        // Inner effect resolved inline — graveyard choice prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Choose the creature (index 0)
        harness.handleGraveyardCardChosen(player1, 0);

        // Grizzly Bears moved from graveyard to hand
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Alice returns Grizzly Bears from graveyard to hand.");
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Decline with -1
        harness.handleGraveyardCardChosen(player1, -1);

        // Grizzly Bears stays in graveyard, not in hand
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing specific creature when multiple are in graveyard")
    void choosesSpecificCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new AngelOfMercy()));
        castAndAcceptMay();

        // Choose Angel of Mercy (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        // Angel of Mercy returned to hand, Grizzly Bears stays in graveyard
        harness.assertInHand(player1, "Angel of Mercy");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Angel of Mercy");
    }

    // ===== Empty / no creatures in graveyard =====

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        // No graveyard set — empty by default
        castAndAcceptMay();

        // Inner effect resolved inline — no graveyard choice since graveyard is empty
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(s -> s.contains("no creature cards in graveyard"));
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has only non-creature cards")
    void noEffectWithOnlyNonCreaturesInGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndAcceptMay();

        // Inner effect resolved inline — no graveyard choice since no creatures
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(s -> s.contains("no creature cards in graveyard"));
        // Holy Day stays in graveyard untouched
        harness.assertInGraveyard(player1, "Holy Day");
    }

    // ===== Invalid choices =====

    @Test
    @DisplayName("Cannot choose non-creature card from graveyard")
    void cannotChooseNonCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new HolyDay(), new GrizzlyBears()));
        castAndAcceptMay();

        // Index 0 is HolyDay (instant, not creature) — not a valid choice
        int logSizeBefore = gd.gameLog.size();
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        assertThat(gd.gameLog).hasSize(logSizeBefore);
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Stack is empty after full resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gravedigger remains on battlefield after returning a creature")
    void gravediggerRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndAcceptMay();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Gravedigger");
    }
}

