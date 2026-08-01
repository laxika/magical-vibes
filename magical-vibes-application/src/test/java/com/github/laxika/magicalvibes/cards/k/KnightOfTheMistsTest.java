package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnightOfTheMistsTest extends BaseCardTest {

    private void castKnightOfTheMists() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KnightOfTheMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature — ETB target prompt
    }

    @Test
    @DisplayName("ETB prompts for a Knight target after entry (can target itself)")
    void etbPromptsForKnightTargetIncludingSelf() {
        castKnightOfTheMists();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID selfId = harness.getPermanentId(player1, "Knight of the Mists");
        assertThat(choice.validIds()).contains(selfId);
    }

    @Test
    @DisplayName("Paying {U} leaves the targeted Knight on the battlefield")
    void payingSavesTheTargetedKnight() {
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        UUID opponentKnightId = harness.getPermanentId(player2, "Zhalfirin Knight");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities(); // resolve ETB → may-pay prompt

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Zhalfirin Knight");
        harness.assertOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("Declining payment destroys the targeted Knight; can't be regenerated")
    void decliningDestroysTargetedKnight() {
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        UUID opponentKnightId = harness.getPermanentId(player2, "Zhalfirin Knight");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities(); // resolve ETB → may-pay prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player2, "Zhalfirin Knight");
        harness.assertOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("With no other Knights, declining destroys itself")
    void decliningWithOnlySelfDestroysSelf() {
        castKnightOfTheMists();
        UUID selfId = harness.getPermanentId(player1, "Knight of the Mists");
        harness.handlePermanentChosen(player1, selfId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Knight of the Mists");
    }

    @Test
    @DisplayName("Accepting without mana falls through to destroy")
    void acceptingWithoutManaDestroys() {
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        UUID opponentKnightId = harness.getPermanentId(player2, "Zhalfirin Knight");

        castKnightOfTheMists();
        harness.handlePermanentChosen(player1, opponentKnightId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // no {U} in pool

        harness.assertNotOnBattlefield(player2, "Zhalfirin Knight");
    }
}
