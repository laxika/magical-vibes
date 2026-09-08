package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KioraTheCrashingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("+1 prevents all damage to and by an opponent's targeted permanent")
    void plusOnePreventsDamageToAndByTargetPermanent() {
        harness.setLife(player1, 20);
        addReadyKiora(player1, 3);
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();

        target.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("+1 cannot target a permanent its controller controls")
    void plusOneCannotTargetOwnPermanent() {
        addReadyKiora(player1, 3);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-1 draws a card and grants an additional land play")
    void minusOneDrawsAndGrantsAdditionalLandPlay() {
        Permanent kiora = addReadyKiora(player1, 3);
        Forest forest = new Forest();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(forest);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("-5 creates a Kraken emblem that triggers at the controller's end step")
    void minusFiveCreatesKrakenEmblem() {
        Permanent kiora = addReadyKiora(player1, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(kiora.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);

        advanceIntoEndStep(player1);

        List<Permanent> krakens = findPermanents(player1, "Kraken");
        assertThat(krakens).hasSize(1);
        Permanent kraken = krakens.getFirst();
        assertThat(kraken.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(kraken.getCard().getSubtypes()).contains(CardSubtype.KRAKEN);
        assertThat(gqs.getEffectivePower(gd, kraken)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, kraken)).isEqualTo(9);
    }

    @Test
    @DisplayName("Kiora's emblem does not trigger at an opponent's end step")
    void emblemDoesNotTriggerOnOpponentsEndStep() {
        addReadyKiora(player1, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceIntoEndStep(player2);

        assertThat(findPermanents(player1, "Kraken")).isEmpty();
    }

    private void advanceIntoEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReadyKiora(Player player, int loyalty) {
        Permanent perm = new Permanent(new KioraTheCrashingWave());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
