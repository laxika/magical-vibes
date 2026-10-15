package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumberingLaundry.class, GraniteWitness.class})
class LumberingLaundryTest extends BaseCardTest {

    @Test
    void controllerCanSeeOpposingFaceDownCreatureUntilEndOfTurn() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isEmpty();
    }

    @Test
    void permissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent faceDownCreature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        faceDownCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearMessages();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isEmpty();
    }

    @Test
    void canCastFaceDownForThreeMana() {
        harness.setHand(player1, List.of(new LumberingLaundry()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lumbering Laundry").isFaceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void canTurnFaceUpForFiveManaAndThenActivateLookAbility() {
        Permanent laundry = harness.addToBattlefieldAndReturn(player1, new LumberingLaundry());
        laundry.setFaceDownAsDisguised();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        opposingCreature.setFaceDownAsDisguised();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.turnFaceUp(player1, 0);

        assertThat(laundry.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isNotEmpty();
    }

    @Test
    void cannotTurnFaceUpWithOnlyFourMana() {
        Permanent laundry = harness.addToBattlefieldAndReturn(player1, new LumberingLaundry());
        laundry.setFaceDownAsDisguised();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(laundry.isFaceDown()).isTrue();
    }

    @Test
    void lookPermissionIsNotGrantedBeforeAbilityResolves() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        opposingCreature.setFaceDownAsDisguised();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isEmpty();
        harness.passBothPriorities();
        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isNotEmpty();
    }

    @Test
    void lookPermissionPersistsAfterSourceLeavesBattlefieldAndCoversLaterCreatures() {
        addCreatureReady(player1, new LumberingLaundry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        opposingCreature.setFaceDownAsDisguised();
        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isNotEmpty();
        assertThat(opposingCreature.isFaceDown()).isTrue();
        assertThat(harness.getConn2().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isEmpty();
    }

    @Test
    void permissionDoesNotRevealFaceDownNoncreatures() {
        addCreatureReady(player1, new LumberingLaundry());
        Permanent faceDownLand = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        faceDownLand.setFaceDown(0, 0, Set.of(CardType.LAND));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("\"name\":\"Granite Witness\""))
                .isEmpty();
    }
}
