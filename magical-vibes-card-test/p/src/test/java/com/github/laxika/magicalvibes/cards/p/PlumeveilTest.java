package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.SilkbindFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Plumeveil.class, SilkbindFaerie.class, SafeholdElite.class})
class PlumeveilTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void canFlashInDuringOpponentsCombatWithAnyWhiteBluePayment(int whiteMana) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Plumeveil()));
        harness.addMana(player1, ManaColor.WHITE, whiteMana);
        harness.addMana(player1, ManaColor.BLUE, 3 - whiteMana);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Plumeveil");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void defenderPreventsAttackingEvenAfterSummoningSicknessEnds() {
        addCreatureReady(player1, new Plumeveil());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void canBlockFlyingCreatureWhileSummoningSick() {
        addCreatureReady(player1, new SilkbindFaerie());
        Permanent plumeveil = harness.addToBattlefieldAndReturn(player2, new Plumeveil());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(plumeveil.isBlocking()).isTrue();
        resolveCombat();
        harness.assertInGraveyard(player1, "Silkbind Faerie");
        harness.assertOnBattlefield(player2, "Plumeveil");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingAndDefenderDoNotPreventBlockingGroundCreature() {
        addCreatureReady(player1, new SafeholdElite());
        Permanent plumeveil = harness.addToBattlefieldAndReturn(player2, new Plumeveil());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(plumeveil.isBlocking()).isTrue();
    }
}
