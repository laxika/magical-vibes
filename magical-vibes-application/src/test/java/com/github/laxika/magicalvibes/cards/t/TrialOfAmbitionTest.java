package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrialOfAmbition.class, CartoucheOfSolidarity.class, ThoseWhoServe.class, GiantSpider.class})
class TrialOfAmbitionTest extends BaseCardTest {

    private void castTrialTargeting(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new TrialOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB: target opponent chooses which of their creatures to sacrifice")
    void opponentChoosesSacrifice() {
        harness.addToBattlefield(player2, new ThoseWhoServe());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castTrialTargeting(player2.getId());

        harness.passBothPriorities(); // resolve enchantment -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger -> opponent's sacrifice choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, giant.getId());

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Those Who Serve");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("ETB: opponent with one creature sacrifices it")
    void opponentWithOneCreatureSacrifices() {
        harness.addToBattlefield(player2, new ThoseWhoServe());

        castTrialTargeting(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Those Who Serve");
        harness.assertInGraveyard(player2, "Those Who Serve");
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new TrialOfAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns to hand when a Cartouche you control enters")
    void bouncesWhenAllyCartoucheEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.addToBattlefield(player1, new TrialOfAmbition());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve aura (queues its ETB + Trial's bounce)
        harness.passBothPriorities(); // resolve a triggered ability
        harness.passBothPriorities(); // resolve the other triggered ability

        harness.assertNotOnBattlefield(player1, "Trial of Ambition");
        harness.assertInHand(player1, "Trial of Ambition");
    }

    @Test
    @DisplayName("Does not return when a Cartouche enters under an opponent's control")
    void staysWhenOpponentCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfAmbition());

        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player2, 0, opponentBears.getId());
        harness.passBothPriorities(); // resolve aura
        harness.passBothPriorities(); // resolve aura's ETB token trigger

        harness.assertOnBattlefield(player1, "Trial of Ambition");
    }

    @Test
    @DisplayName("An opponent without creatures sacrifices nothing")
    void opponentWithoutCreaturesSacrificesNothing() {
        harness.addToBattlefield(player1, new ThoseWhoServe());
        harness.addToBattlefield(player2, new TrialOfAmbition());

        castTrialTargeting(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Those Who Serve");
        harness.assertOnBattlefield(player1, "Trial of Ambition");
        harness.assertOnBattlefield(player2, "Trial of Ambition");
    }

    @Test
    @DisplayName("A non-Cartouche enchantment does not return an existing Trial")
    void staysWhenNonCartoucheEnters() {
        harness.addToBattlefield(player1, new TrialOfAmbition());

        castTrialTargeting(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Trial of Ambition"))
                .hasSize(2);
        harness.assertNotInHand(player1, "Trial of Ambition");
    }

    @Test
    @DisplayName("One Cartouche returns every Trial its controller controls")
    void cartoucheReturnsMultipleTrials() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.addToBattlefield(player1, new TrialOfAmbition());
        harness.addToBattlefield(player1, new TrialOfAmbition());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Trial of Ambition");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Trial of Ambition"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A controlled Trial returns to its owner rather than its controller")
    void returnsToOwnersHand() {
        TrialOfAmbition trial = new TrialOfAmbition();
        trial.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, trial);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Trial of Ambition");
        harness.assertInHand(player2, "Trial of Ambition");
        harness.assertNotInHand(player1, "Trial of Ambition");
    }
}
