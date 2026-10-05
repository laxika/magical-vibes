package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.Ephemerate;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaradorGhostChieftain.class, GrizzlyBears.class, LightningBolt.class, WalkingCorpse.class, Ephemerate.class})
class KaradorGhostChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each creature card in its controller's graveyard")
    void reducesCostForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        prepareMainPhase();
        harness.castFromHand(player1, new KaradorGhostChieftain(), "{3}{W}{B}{G}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Allows one creature spell from the controller's graveyard each turn")
    void allowsOneCreatureFromGraveyardEachTurn() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the graveyard")
    void cannotCastNoncreatureFromGraveyard() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());

        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotCountItselfWhenCastFromGraveyard() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new KaradorGhostChieftain(), new WalkingCorpse()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Karador, Ghost Chieftain");
    }

    @Test
    void excessCreaturesDoNotReduceColoredManaRequirements() {
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse(),
                new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new KaradorGhostChieftain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void ignoresCreaturesInOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new WalkingCorpse()));
        prepareMainPhase();

        harness.castFromHand(player1, new KaradorGhostChieftain(), "{5}{W}{B}{G}");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotUsePermissionDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    void permissionDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Walking Corpse");
    }

    @Test
    void unsuccessfulCastDoesNotConsumePermission() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.setHand(player1, List.of());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void permissionRefreshesOnNextControllerTurn() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setLibrary(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void returningKaradorGrantsAnotherCastInSameTurn() {
        var karador = harness.addToBattlefieldAndReturn(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, karador.getId());
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Karador, Ghost Chieftain");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastNonflashCreatureWithSpellOnStack() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        prepareMainPhase();
        harness.castFromHand(player1, new WalkingCorpse(), "{1}{B}");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
