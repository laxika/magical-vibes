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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(oppHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
    }

    @Test
    @DisplayName("-2 cannot target a noncreature permanent")
    void minusTwoCannotTargetNoncreature() {
        addReadyJace(player1);
        Permanent jaceSelf = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Jace, Unraveler of Secrets"))
                .findFirst().orElseThrow();

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
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst())
                .isInstanceOf(CounterOpponentFirstSpellEachTurnEffect.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Jace, Unraveler of Secrets"));
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
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Cannot activate -8 with insufficient loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyJace(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyJace(com.github.laxika.magicalvibes.model.Player player) {
        JaceUnravelerOfSecrets card = new JaceUnravelerOfSecrets();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
