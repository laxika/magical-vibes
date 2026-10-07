package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SusurianVoidborn.class, GrizzlyBears.class, LightningBolt.class, MindStone.class,
        Naturalize.class, Ornithopter.class, LiquimetalCoating.class})
class SusurianVoidbornTest extends BaseCardTest {

    @Test
    @DisplayName("When Susurian Voidborn dies, target opponent loses 1 life and its controller gains 1 life")
    void selfDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new SusurianVoidborn());

        destroyWithLightningBolt(player1, "Susurian Voidborn");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A non-artifact creature you control dying triggers Susurian Voidborn")
    void nonArtifactCreatureDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        harness.addToBattlefield(player1, new GrizzlyBears());

        destroyWithLightningBolt(player1, "Grizzly Bears");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An artifact you control being put into a graveyard triggers Susurian Voidborn")
    void artifactDeathDrainsTargetOpponent() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        harness.addToBattlefield(player1, new MindStone());

        destroyArtifact("Mind Stone");
        resolveDrain(player2.getId());

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An artifact creature dying triggers Susurian Voidborn only once")
    void artifactCreatureDeathDoesNotTriggerTwice() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        harness.addToBattlefield(player1, new Ornithopter());

        destroyArtifact("Ornithopter");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        resolveDrain(player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Susurian Voidborn")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Warp casts Susurian Voidborn for {B} and exiles it at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        SusurianVoidborn voidborn = new SusurianVoidborn();
        harness.setHand(player1, List.of(voidborn));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(voidborn.getId())).isNotNull();
    }

    @Test
    void selfDeathAsAnArtifactTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addToBattlefield(player1, new SusurianVoidborn());
        UUID voidbornId = harness.getPermanentId(player1, "Susurian Voidborn");
        harness.activateAbility(player1, 0, null, voidbornId);
        harness.passBothPriorities();

        destroyWithLightningBolt(player1, "Susurian Voidborn");
        resolveDrain(player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    void opponentArtifactDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        harness.addToBattlefield(player2, new MindStone());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        setupPlayer2Active();

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Mind Stone"));

        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void drainCannotTargetItsController() {
        harness.addToBattlefield(player1, new SusurianVoidborn());
        destroyWithLightningBolt(player1, "Susurian Voidborn");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveDrain(player2.getId());

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void warpedCreatureDyingBeforeEndStepDrainsAndStaysInGraveyard() {
        SusurianVoidborn voidborn = new SusurianVoidborn();
        harness.setHand(player1, List.of(voidborn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        destroyWithLightningBolt(player1, "Susurian Voidborn");
        resolveDrain(player2.getId());
        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Susurian Voidborn");
        assertThat(gd.findExiledCard(voidborn.getId())).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void normalCastDoesNotExileAtEndStep() {
        harness.castFromHand(player1, new SusurianVoidborn(), "{2}{B}");
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Susurian Voidborn");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void warpExileDoesNotDrainAndPermitsNormalCastingOnALaterTurn() {
        SusurianVoidborn voidborn = new SusurianVoidborn();
        harness.setHand(player1, List.of(voidborn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(voidborn.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Susurian Voidborn");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, voidborn.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Susurian Voidborn");
        assertThat(gd.findExiledCard(voidborn.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyWithLightningBolt(com.github.laxika.magicalvibes.model.Player controller,
                                           String permanentName) {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupPlayer2Active();

        UUID permanentId = harness.getPermanentId(controller, permanentName);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void destroyArtifact(String permanentName) {
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        setupPlayer2Active();

        UUID permanentId = harness.getPermanentId(player1, permanentName);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    private void resolveDrain(UUID targetId) {
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
