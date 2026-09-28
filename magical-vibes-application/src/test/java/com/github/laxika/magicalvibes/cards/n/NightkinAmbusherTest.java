package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightkinAmbusher.class, GrizzlyBears.class, Shock.class})
class NightkinAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives four rad counters to the chosen player")
    void entersGivesFourRadCountersToTargetPlayer() {
        harness.enterBattlefieldAndReturn(player1, new NightkinAmbusher());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent ambusher = new Permanent(new NightkinAmbusher());
        ambusher.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ambusher);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ambusher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Can't be blocked while the defending player has a rad counter")
    void cantBeBlockedWhenDefenderHasRadCounters() {
        gd.playerRadCounters.put(player2.getId(), 1);
        Permanent ambusher = addReadyAttacker();
        addReadyBlocker();

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        assertThat(ambusher.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked when the defending player has no rad counters")
    void canBeBlockedWhenDefenderHasNoRadCounters() {
        harness.setLife(player2, 20);
        addReadyAttacker();
        addReadyBlocker();

        beginBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addReadyAttacker() {
        Permanent ambusher = new Permanent(new NightkinAmbusher());
        ambusher.setSummoningSick(false);
        ambusher.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(ambusher);
        return ambusher;
    }

    private void addReadyBlocker() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
