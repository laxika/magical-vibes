package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KapshoKitefins.class, RuneclawBear.class})
class KapshoKitefinsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield taps target creature an opponent controls")
    void selfEntryTapsOpponentCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.castFromHand(player1, new KapshoKitefins(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another creature entering taps target creature an opponent controls")
    void anotherCreatureEntryTapsOpponentCreature() {
        harness.addToBattlefield(player1, new KapshoKitefins());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The triggered ability cannot target a creature controlled by its controller")
    void cannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.castFromHand(player1, new KapshoKitefins(), "{4}{U}{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, own.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isFalse();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opposing creature entering does not trigger Kitefins")
    void opponentCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KapshoKitefins());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.enterBattlefieldAndReturn(player2, new RuneclawBear());
        resolveAllTriggers();

        assertThat(victim.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Kitefins can be cast without any opposing creatures")
    void canEnterWithoutLegalTargets() {
        harness.castFromHand(player1, new KapshoKitefins(), "{4}{U}{U}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Kapsho Kitefins")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Runeclaw Bear")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An already tapped opposing creature is a legal target")
    void canTargetTappedCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        victim.tap();

        harness.castFromHand(player1, new KapshoKitefins(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ally entry trigger resolves after Kitefins leaves the battlefield")
    void allyEntryTriggerSurvivesSourceRemoval() {
        Permanent kitefins = harness.addToBattlefieldAndReturn(player1, new KapshoKitefins());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.castFromHand(player1, new RuneclawBear(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());

        gd.playerBattlefields.get(player1.getId()).remove(kitefins);
        gd.playerGraveyards.get(player1.getId()).add(kitefins.getCard());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }
}
