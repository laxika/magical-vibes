package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseTraders.class, MoorlandInquisitor.class, Forest.class})
class CorpseTradersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the chosen creature and targets the opponent")
    void activatingSacrificesCreatureAndTargetsOpponent() {
        addReadyCorpseTraders(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        UUID inquisitorId = harness.getPermanentId(player1, "Moorland Inquisitor");
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, inquisitorId);

        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertOnBattlefield(player1, "Corpse Traders");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving reveals the opponent's hand and discards the chosen card")
    void resolvingDiscardsChosenCard() {
        addReadyCorpseTraders(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        UUID inquisitorId = harness.getPermanentId(player1, "Moorland Inquisitor");
        harness.setHand(player2, new ArrayList<>(List.of(new MoorlandInquisitor(), new Forest())));
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, inquisitorId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactlyInAnyOrder(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Moorland Inquisitor");
    }

    @Test
    @DisplayName("Cannot activate at instant speed")
    void cannotActivateAtInstantSpeed() {
        addReadyCorpseTraders(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReadyCorpseTraders(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyForSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addReadyCorpseTraders(Player player) {
        addCreatureReady(player, new CorpseTraders());
    }

    @Test
    @DisplayName("A summoning-sick Corpse Traders can sacrifice itself and its ability still resolves")
    void canSacrificeItselfWhileSummoningSick() {
        Permanent traders = harness.addToBattlefieldAndReturn(player1, new CorpseTraders());
        traders.setSummoningSick(true);
        traders.tap();
        harness.setHand(player2, new ArrayList<>(List.of(new MoorlandInquisitor(), new Forest())));
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Corpse Traders");
        harness.assertInGraveyard(player1, "Corpse Traders");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent activation or refund the sacrifice")
    void resolvesAgainstEmptyHand() {
        Permanent traders = harness.addToBattlefieldAndReturn(player1, new CorpseTraders());
        harness.setHand(player2, new ArrayList<>());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Corpse Traders");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate during your combat phase")
    void cannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new CorpseTraders());
        readyForSorcerySpeed();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addReadyCorpseTraders(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player2, new ArrayList<>());
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Moorland Inquisitor"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        harness.passBothPriorities();
    }
}
