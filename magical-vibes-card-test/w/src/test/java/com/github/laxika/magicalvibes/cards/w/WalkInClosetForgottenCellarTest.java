package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GraspingLongneck;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkInClosetForgottenCellar.class, Forest.class, LightningBolt.class, GraspingLongneck.class})
class WalkInClosetForgottenCellarTest extends BaseCardTest {

    @Test
    void walkInClosetAllowsPlayingALandFromTheGraveyard() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        castRoom(0, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void forgottenCellarAllowsCastingFromTheGraveyardAndExilesTheSpell() {
        LightningBolt lightningBolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(lightningBolt));
        castRoom(1, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lightningBolt);
    }

    @Test
    void lockedWalkInClosetDoesNotAllowPlayingGraveyardLands() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castRoom(1, 5);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void puttingTheRoomOntoTheBattlefieldWithoutCastingUnlocksNeitherDoor() {
        harness.setGraveyard(player1, List.of(new Forest(), new GraspingLongneck()));
        harness.enterBattlefieldAndReturn(player1, new WalkInClosetForgottenCellar());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void unlockingWalkInClosetAfterForgottenCellarAllowsPlayingALand() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castRoom(1, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.unlockRoomDoor(player1, 0, 0);
        resolveAllTriggers();
        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void walkInClosetDoesNotGrantAnAdditionalLandPlay() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        castRoom(0, 3);
        harness.playGraveyardLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void walkInClosetDoesNotAllowLandPlaysOutsideAMainPhase() {
        harness.setGraveyard(player1, List.of(new Forest()));
        castRoom(0, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void walkInClosetDoesNotAllowCastingGraveyardSpells() {
        harness.setGraveyard(player1, List.of(new GraspingLongneck()));
        castRoom(0, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grasping Longneck");
    }

    @Test
    void unlockingForgottenCellarGrantsPermissionOnlyAfterTheTriggerResolves() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bolt));
        castRoom(0, 3);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.unlockRoomDoor(player1, 0, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bolt);
    }

    @Test
    void forgottenCellarAllowsMultipleSpellsAndExilesARecastCreatureWhenItDies() {
        GraspingLongneck first = new GraspingLongneck();
        GraspingLongneck second = new GraspingLongneck();
        harness.setGraveyard(player1, List.of(first, second));
        castRoom(1, 5);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, findPermanents(player1, "Grasping Longneck").getFirst().getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grasping Longneck")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void forgottenCellarDoesNotWaiveManaCosts() {
        harness.setGraveyard(player1, List.of(new GraspingLongneck()));
        castRoom(1, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grasping Longneck");
    }

    @Test
    void forgottenCellarDoesNotAllowCreatureCastingAtInstantSpeed() {
        harness.setGraveyard(player1, List.of(new GraspingLongneck()));
        castRoom(1, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grasping Longneck");
    }

    @Test
    void forgottenCellarExilesSpellsCastFromHandButDoesNotAffectTheOpponent() {
        LightningBolt ownSpell = new LightningBolt();
        LightningBolt opposingSpell = new LightningBolt();
        castRoom(1, 5);
        harness.setHand(player1, List.of(ownSpell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(opposingSpell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownSpell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSpell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void forgottenCellarPermissionAndExileReplacementExpireAtEndOfTurn() {
        GraspingLongneck creature = new GraspingLongneck();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(creature));
        castRoom(1, 5);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, bolt);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void forgottenCellarEffectsPersistAfterTheRoomLeavesTheBattlefield() {
        GraspingLongneck creature = new GraspingLongneck();
        harness.setGraveyard(player1, List.of(creature));
        castRoom(1, 5);
        var room = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, room));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grasping Longneck");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anySatisfy(card -> assertThat(card.getId()).isEqualTo(room.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castRoom(int doorIndex, int manaAmount) {
        harness.setHand(player1, List.of(new WalkInClosetForgottenCellar()));
        harness.addMana(player1, ManaColor.GREEN, manaAmount);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        resolveAllTriggers();
    }
}
