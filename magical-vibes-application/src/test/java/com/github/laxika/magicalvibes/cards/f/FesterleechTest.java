package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Festerleech.class, GravestoneStrider.class})
class FesterleechTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player mills two cards from the controller only")
    void combatDamageMillsTwoCards() {
        Permanent leech = addCreatureReady(player1, new Festerleech());
        leech.setAttacking(true);
        harness.setLibrary(player1, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));
        harness.setLibrary(player2, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A blocked Festerleech does not mill")
    void blockedFesterleechDoesNotMill() {
        Permanent leech = addCreatureReady(player1, new Festerleech());
        leech.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GravestoneStrider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));
        harness.setLibrary(player2, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));

        resolveCombat();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The activated ability gives +2/+2 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent leech = addCreatureReady(player1, new Festerleech());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability can be used only once each turn")
    void activatedAbilityOnlyOnceEachTurn() {
        addCreatureReady(player1, new Festerleech());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Combat damage mills the controller's remaining card when fewer than two remain")
    void combatDamageWithOneCardInControllerLibrary() {
        Permanent leech = addCreatureReady(player1, new Festerleech());
        leech.setAttacking(true);
        harness.setLibrary(player1, List.of(new GravestoneStrider()));
        harness.setLibrary(player2, List.of(new GravestoneStrider(), new GravestoneStrider()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Player two's Festerleech mills player two when it hits player one")
    void combatDamageMillsOtherController() {
        Permanent leech = addCreatureReady(player2, new Festerleech());
        leech.setAttacking(true);
        harness.setLibrary(player1, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));
        harness.setLibrary(player2, List.of(new GravestoneStrider(), new GravestoneStrider(), new GravestoneStrider()));

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The activation limit applies before the first activation resolves")
    void cannotActivateAgainWithFirstActivationOnStack() {
        Permanent leech = addCreatureReady(player1, new Festerleech());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped summoning-sick Festerleech can activate on the opponent's turn")
    void canActivateWhileTappedAndSummoningSickOnOpponentsTurn() {
        harness.addToBattlefield(player1, new Festerleech());
        Permanent leech = gd.playerBattlefields.get(player1.getId()).getFirst();
        leech.setSummoningSick(true);
        leech.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, leech)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, leech)).isEqualTo(3);
        assertThat(leech.isTapped()).isTrue();
    }
}
