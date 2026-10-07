package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinybonesBaubleBurglar.class, GrizzlyBears.class, LightningBolt.class,
        Swamp.class, WitnessProtection.class})
class TinybonesBaubleBurglarTest extends BaseCardTest {

    @Test
    void exilesAnOpponentsDiscardWithAStashCounterAndLetsControllerCastIt() {
        Card discarded = new GrizzlyBears();
        addCreatureReady(player1, new TinybonesBaubleBurglar());
        harness.setHand(player2, List.of(discarded));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, discarded.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void stashPermissionSurvivesTheTinybonesThatCreatedItLeavingTheBattlefield() {
        Card discarded = new GrizzlyBears();
        TinybonesBaubleBurglar tinybones = new TinybonesBaubleBurglar();
        addCreatureReady(player1, tinybones);
        UUID tinybonesPermanentId = harness.getPermanentId(player1, "Tinybones, Bauble Burglar");
        harness.setHand(player2, List.of(discarded));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, tinybonesPermanentId);
        harness.assertNotOnBattlefield(player1, "Tinybones, Bauble Burglar");

        harness.addToBattlefield(player1, new TinybonesBaubleBurglar());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, discarded.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canPlayAStashedLandButItUsesTheNormalLandPlay() {
        Card discarded = new Swamp();
        stashOpponentCard(discarded);

        harness.castFromExile(player1, discarded.getId());

        harness.assertOnBattlefield(player1, "Swamp");
        harness.setHand(player1, List.of(new Swamp()));
        prepareMainPhase();
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastEvenAStashedInstantDuringTheOpponentsTurn() {
        Card discarded = new LightningBolt();
        stashOpponentCard(discarded);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
    }

    @Test
    void stashPermissionDoesNotGiveCreaturesFlash() {
        Card discarded = new GrizzlyBears();
        stashOpponentCard(discarded);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
    }

    @Test
    void cannotActivateOutsideSorceryTiming() {
        addCreatureReady(player1, new TinybonesBaubleBurglar());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void cannotPlayStashedCardsWithoutAnActiveTinybones() {
        Card discarded = new GrizzlyBears();
        stashOpponentCard(discarded);
        UUID tinybonesId = harness.getPermanentId(player1, "Tinybones, Bauble Burglar");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, tinybonesId);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
    }

    @Test
    void losingAbilitiesRemovesThePermissionToPlayStashedCards() {
        Card discarded = new GrizzlyBears();
        stashOpponentCard(discarded);
        UUID tinybonesId = harness.getPermanentId(player1, "Tinybones, Bauble Burglar");
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, tinybonesId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Legitimate Businessperson");
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
    }

    @Test
    void canCastAStashedInstantDuringItsControllersUpkeep() {
        Card discarded = new LightningBolt();
        stashOpponentCard(discarded);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromExile(player1, discarded.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void cannotPlayAnOwnedCardStashedByAnOpponentsTinybones() {
        Card discarded = new GrizzlyBears();
        addCreatureReady(player2, new TinybonesBaubleBurglar());
        harness.setHand(player1, List.of(discarded));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);

        harness.addToBattlefield(player1, new TinybonesBaubleBurglar());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
    }

    @Test
    void discardTriggerStillExilesTheCardAfterTinybonesLeaves() {
        Card discarded = new GrizzlyBears();
        addCreatureReady(player1, new TinybonesBaubleBurglar());
        UUID tinybonesId = harness.getPermanentId(player1, "Tinybones, Bauble Burglar");
        harness.setHand(player2, List.of(discarded, new LightningBolt()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player2, 0, tinybonesId);
        harness.assertNotOnBattlefield(player1, "Tinybones, Bauble Burglar");
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void activatingAgainstAnEmptyHandStillPaysTheTapCost() {
        addCreatureReady(player1, new TinybonesBaubleBurglar());
        harness.setHand(player2, List.of());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tinybones, Bauble Burglar").isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void stashOpponentCard(Card discarded) {
        addCreatureReady(player1, new TinybonesBaubleBurglar());
        harness.setHand(player2, List.of(discarded));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(discarded);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
