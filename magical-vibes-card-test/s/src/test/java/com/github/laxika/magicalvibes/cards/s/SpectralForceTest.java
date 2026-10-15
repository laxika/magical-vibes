package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.cards.e.Evangelize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralForce.class, BenalishCavalry.class, DrudgeReavers.class, Evangelize.class})
class SpectralForceTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while the defender controls no black permanents locks Spectral Force's next untap")
    void locksUntapWhenDefenderHasNoBlackPermanents() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BenalishCavalry());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    @DisplayName("No untap lock when the defending player controls a black permanent")
    void noLockWhenDefenderHasBlackPermanent() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new DrudgeReavers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the defending player's permanents matter")
    void controllersBlackPermanentIsIgnored() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player1, new DrudgeReavers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A black permanent entering before resolution stops the intervening-if ability")
    void blackPermanentEnteringBeforeResolutionPreventsLock() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BenalishCavalry());

        declareAttackers(player1, List.of(0));
        addCreatureReady(player2, new DrudgeReavers());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    void blackPermanentLeavingAfterAttackDoesNotCreateTrigger() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        Permanent reavers = addCreatureReady(player2, new DrudgeReavers());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(reavers);
        gd.playerGraveyards.get(player2.getId()).add(reavers.getCard());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    void blackPermanentEnteringAfterResolutionDoesNotRemoveLock() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BenalishCavalry());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        addCreatureReady(player2, new DrudgeReavers());

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    void restrictionExpiresEvenIfForceIsAlreadyUntapped() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BenalishCavalry());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        force.untap();

        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
        force.tap();
        harness.performUntapStep(player1);
        assertThat(force.isTapped()).isFalse();
    }

    @Test
    void changingControlAfterResolutionDoesNotLockNewControllersUntap() {
        Permanent force = addCreatureReady(player1, new SpectralForce());
        addCreatureReady(player2, new BenalishCavalry());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(force.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Evangelize()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castSorcery(player2, 0, player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.handlePermanentChosen(player1, force.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(force);

        harness.performUntapStep(player2);
        assertThat(force.isTapped()).isFalse();
    }
}
