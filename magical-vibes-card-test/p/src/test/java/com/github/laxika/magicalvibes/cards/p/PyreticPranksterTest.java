package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlisteningGoremonger;
import com.github.laxika.magicalvibes.cards.a.AxgardArtisan;
import com.github.laxika.magicalvibes.cards.b.BeamtownBeatstick;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        PyreticPrankster.class,
        GlisteningGoremonger.class,
        AxgardArtisan.class,
        BeamtownBeatstick.class,
        Forest.class
})
class PyreticPranksterTest extends BaseCardTest {

    @Test
    void transformsByPayingBlackMana() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prankster.isTransformed()).isTrue();
        assertThat(prankster.getCard()).isInstanceOf(GlisteningGoremonger.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prankster.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedFaceDeathTriggerMakesEachOpponentSacrificeAnArtifactOrCreature() {
        Permanent goremonger = addTransformedPrankster();
        harness.addToBattlefield(player2, new AxgardArtisan());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BeamtownBeatstick());
        harness.addToBattlefield(player2, new Forest());

        goremonger.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Beamtown Beatstick");
        harness.assertOnBattlefield(player2, "Axgard Artisan");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void dyingAfterResolvingTransformationTriggersAndReturnsFrontFaceToGraveyard() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addToBattlefield(player2, new AxgardArtisan());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prankster.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Glistening Goremonger");

        prankster.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Glistening Goremonger");
        harness.assertNotOnBattlefield(player2, "Axgard Artisan");
        harness.assertInGraveyard(player1, "Pyretic Prankster");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTransformOutsideMainPhase() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prankster.isTransformed()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTransformDuringOpponentsMainPhase() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prankster.isTransformed()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTransformationIsOnStack() {
        Permanent prankster = addPrankster();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(prankster.isTransformed()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(prankster.isTransformed()).isTrue();
    }

    @Test
    void frontFaceDeathDoesNotMakeOpponentSacrifice() {
        Permanent prankster = addPrankster();
        harness.addToBattlefield(player2, new AxgardArtisan());

        prankster.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Pyretic Prankster");
        harness.assertOnBattlefield(player2, "Axgard Artisan");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCanChooseCreatureAndControllerKeepsTheirPermanents() {
        Permanent goremonger = addTransformedPrankster();
        harness.addToBattlefield(player1, new AxgardArtisan());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxgardArtisan());
        harness.addToBattlefield(player2, new BeamtownBeatstick());

        goremonger.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Axgard Artisan");
        harness.assertOnBattlefield(player2, "Beamtown Beatstick");
        harness.assertOnBattlefield(player1, "Axgard Artisan");
    }

    @Test
    void onlyEligiblePermanentIsSacrificedWithoutAChoice() {
        Permanent goremonger = addTransformedPrankster();
        harness.addToBattlefield(player2, new BeamtownBeatstick());
        harness.addToBattlefield(player2, new Forest());

        goremonger.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Beamtown Beatstick");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithOnlyLandSacrificesNothing() {
        Permanent goremonger = addTransformedPrankster();
        harness.addToBattlefield(player2, new Forest());

        goremonger.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPrankster() {
        return harness.addToBattlefieldAndReturn(player1, new PyreticPrankster());
    }

    private Permanent addTransformedPrankster() {
        Permanent prankster = addPrankster();
        prankster.setCard(prankster.getCard().getBackFaceCard());
        prankster.setTransformed(true);
        return prankster;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
