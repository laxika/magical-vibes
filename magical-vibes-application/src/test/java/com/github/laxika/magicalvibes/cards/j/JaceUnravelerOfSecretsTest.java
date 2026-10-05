package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaceUnravelerOfSecrets.class, GrizzlyBears.class, Shock.class})
class JaceUnravelerOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("+1 scries 1 then draws a card")
    void plusOneScriesThenDraws() {
        Permanent jace = addReadyJace(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
    }

    @Test
    @DisplayName("-2 returns target creature to its owner's hand")
    void minusTwoBouncesCreature() {
        Permanent jace = addReadyJace(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        int oppHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(oppHandBefore + 1);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
    }

    @Test
    @DisplayName("-2 cannot target a noncreature permanent")
    void minusTwoCannotTargetNoncreature() {
        addReadyJace(player1);
        Permanent jaceSelf = findPermanent(player1, "Jace, Unraveler of Secrets");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, jaceSelf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 creates emblem that counters opponent's first spell each turn")
    void minusEightCreatesEmblem() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player1, "Jace, Unraveler of Secrets");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Emblem counters opponent's first spell each turn")
    void emblemCountersFirstOpponentSpell() {
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new CounterOpponentFirstSpellEachTurnEffect.Marker()
        ), new JaceUnravelerOfSecrets()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2); // Shock + emblem trigger

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20); // Shock never resolved
    }

    @Test
    @DisplayName("Emblem does not counter opponent's second spell in the same turn")
    void emblemDoesNotCounterSecondSpellSameTurn() {
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new CounterOpponentFirstSpellEachTurnEffect.Marker()
        ), new JaceUnravelerOfSecrets()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First Shock — countered
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        // Second Shock — resolves
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Emblem does not trigger when emblem controller casts a spell")
    void emblemDoesNotTriggerOnControllerSpell() {
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new CounterOpponentFirstSpellEachTurnEffect.Marker()
        ), new JaceUnravelerOfSecrets()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Cannot activate -8 with insufficient loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("+1 draws the next card after putting the top card on the bottom")
    void plusOneDrawsAfterBottoming() {
        addReadyJace(player1);
        Shock bottomed = new Shock();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bottomed, drawn));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomed);
    }

    @Test
    @DisplayName("-2 can return a creature controlled by Jace's controller")
    void minusTwoBouncesOwnCreature() {
        addReadyJace(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An emblem created after the opponent's first spell does not counter their second")
    void emblemCreatedAfterFirstSpellDoesNotCounterSecond() {
        Permanent jace = addReadyJace(player1);
        jace.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The emblem counters the opponent's first spell again on the next turn")
    void emblemTriggersAgainNextTurn() {
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new CounterOpponentFirstSpellEachTurnEffect.Marker()
        ), new JaceUnravelerOfSecrets()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-2 does nothing when its target dies in response, but still costs loyalty")
    void minusTwoTargetDiesInResponse() {
        Permanent jace = addReadyJace(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyJace(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaceUnravelerOfSecrets());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
