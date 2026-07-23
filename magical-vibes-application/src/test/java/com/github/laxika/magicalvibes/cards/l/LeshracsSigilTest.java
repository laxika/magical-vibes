package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new LightningBolt())));
        harness.addMana(player2, ManaColor.GREEN, 2);

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

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Opponent green spell: declining to pay does not discard")
    void opponentGreenSpellDecline() {
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new LightningBolt())));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Non-green opponent spell does not trigger")
    void nonGreenDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Leshrac's Sigil"));
    }

    @Test
    @DisplayName("Controller's own green spell does not trigger")
    void ownGreenDoesNotTrigger() {
        harness.addToBattlefield(player1, new LeshracsSigil());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

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

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leshrac's Sigil"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leshrac's Sigil"));
    }
}
