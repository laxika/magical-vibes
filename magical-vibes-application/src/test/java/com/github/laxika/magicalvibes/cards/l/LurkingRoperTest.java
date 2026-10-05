package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingRoper.class, HillGiantHerdgorger.class})
class LurkingRoperTest extends BaseCardTest {

    @Test
    @DisplayName("Does not untap during its controller's untap step")
    void doesNotUntapDuringControllerUntapStep() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());
        roper.tap();

        harness.performUntapStep(player1);

        assertThat(roper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps when its controller gains life")
    void untapsOnLifeGain() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());
        roper.tap();

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(roper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());
        roper.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new HillGiantHerdgorger(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(roper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain untaps the Roper only when its triggered ability resolves")
    void untapWaitsForTriggerResolution() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());
        roper.tap();
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        assertThat(roper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(roper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each controlled Roper triggers once per life gain event and untaps only itself")
    void eachRoperUntapsOnlyItself() {
        Permanent first = addCreatureReady(player1, new LurkingRoper());
        Permanent second = addCreatureReady(player1, new LurkingRoper());
        Permanent opponent = addCreatureReady(player2, new LurkingRoper());
        Permanent otherCreature = addCreatureReady(player1, new HillGiantHerdgorger());
        first.tap();
        second.tap();
        opponent.tap();
        otherCreature.tap();

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(first.isTapped() ^ second.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Roper still triggers and untaps if tapped before resolution")
    void untappedRoperStillTriggers() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        roper.tap();
        harness.passBothPriorities();

        assertThat(roper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Separate life gain events can untap the Roper repeatedly in the same turn")
    void untapsForEachLifeGainEvent() {
        Permanent roper = addCreatureReady(player1, new LurkingRoper());
        roper.tap();
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(roper.isTapped()).isFalse();
        harness.assertLife(player1, 13);
        roper.tap();

        harness.castFromHand(player1, new HillGiantHerdgorger(), "{4}{G}{G}");
        resolveAllTriggers();

        assertThat(roper.isTapped()).isFalse();
        harness.assertLife(player1, 16);
    }
}
