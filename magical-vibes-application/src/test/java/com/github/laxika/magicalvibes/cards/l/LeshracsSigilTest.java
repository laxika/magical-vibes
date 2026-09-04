package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.Stormbind;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeshracsSigil.class, DarkRitual.class, FyndhornElves.class, Incinerate.class, Stormbind.class})
class LeshracsSigilTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new LeshracsSigil());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent green spell: pay {B}{B}, choose a card from their hand to discard")
    void opponentGreenSpellPayAndDiscard() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new FyndhornElves(), new Incinerate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leshrac's Sigil"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.discardMode()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Incinerate");
        harness.assertNotInHand(player2, "Incinerate");
    }

    @Test
    @DisplayName("Opponent green spell: looking at the hand is private to the controller")
    void opponentGreenSpellLooksAtHandPrivately() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new FyndhornElves(), new Incinerate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Incinerate"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND")).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("looks at") && log.contains("hand"))
                .noneMatch(log -> log.contains("Incinerate"));
    }

    @Test
    @DisplayName("Opponent green spell: declining to pay does not discard")
    void opponentGreenSpellDecline() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new FyndhornElves(), new Incinerate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Incinerate");
        harness.assertNotInGraveyard(player2, "Incinerate");
    }

    @Test
    @DisplayName("Opponent multicolored green spell triggers")
    void opponentMulticoloredGreenSpellTriggers() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Stormbind(), new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leshrac's Sigil"));
    }

    @Test
    @DisplayName("Opponent green spell with an empty hand has no card choice")
    void opponentGreenSpellEmptyHand() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new FyndhornElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Non-green opponent spell does not trigger")
    void nonGreenDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leshrac's Sigil"));
    }

    @Test
    @DisplayName("Controller's own green spell does not trigger")
    void ownGreenDoesNotTrigger() {
        harness.addToBattlefield(player1, new LeshracsSigil());
        harness.setHand(player1, List.of(new FyndhornElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leshrac's Sigil"));
    }

    @Test
    @DisplayName("{B}{B}: return Leshrac's Sigil to its owner's hand")
    void bounceAbilityReturnsToHand() {
        harness.addToBattlefield(player1, new LeshracsSigil());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leshrac's Sigil");
        harness.assertNotOnBattlefield(player1, "Leshrac's Sigil");
    }
}
