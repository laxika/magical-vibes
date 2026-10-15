package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalaceJailer.class, BenalishCavalry.class, Dismember.class})
class PalaceJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters, makes its controller monarch, and exiles an opposing creature")
    void entersAndExilesOpposingCreature() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        UUID cavalryId = harness.getPermanentId(player2, "Benalish Cavalry");

        castPalaceJailer(player1, cavalryId);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Benalish Cavalry");
        assertThat(gd.exileReturnOnOpponentBecomesMonarch).containsKey(player1.getId());
    }

    @Test
    @DisplayName("Returns the exiled creature when an opponent becomes monarch")
    void returnsExiledCreatureWhenOpponentBecomesMonarch() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        UUID cavalryId = harness.getPermanentId(player2, "Benalish Cavalry");
        castPalaceJailer(player1, cavalryId);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID firstJailerId = harness.getPermanentId(player1, "Palace Jailer");
        harness.setHand(player2, List.of(new PalaceJailer()));
        addPalaceJailerMana(player2);
        harness.castCreature(player2, 0, firstJailerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Benalish Cavalry"));
        assertThat(gd.exileReturnOnOpponentBecomesMonarch).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a creature controlled by its own controller")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new BenalishCavalry());
        UUID cavalryId = harness.getPermanentId(player1, "Benalish Cavalry");
        harness.setHand(player1, List.of(new PalaceJailer()));
        addPalaceJailerMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, cavalryId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Becomes monarch even when there is no opposing creature to exile")
    void becomesMonarchWithoutOpposingCreature() {
        harness.castFromHand(player1, new PalaceJailer(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Palace Jailer");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The monarch ability still resolves when the exile target dies in response")
    void becomesMonarchWhenExileTargetBecomesIllegal() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        UUID targetId = harness.getPermanentId(player2, "Benalish Cavalry");
        harness.setHand(player1, List.of(new PalaceJailer()));
        addPalaceJailerMana(player1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        destroyWithDismember(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Leaving the battlefield before its abilities resolve does not prevent exile")
    void exilesCreatureAfterJailerDiesInResponse() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        UUID targetId = harness.getPermanentId(player2, "Benalish Cavalry");
        harness.setHand(player1, List.of(new PalaceJailer()));
        addPalaceJailerMana(player1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        destroyWithDismember(harness.getPermanentId(player1, "Palace Jailer"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Palace Jailer");
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Benalish Cavalry");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The exiled creature stays exiled after Jailer dies and returns on a later monarch change")
    void returnsCreatureAfterJailerHasLeftBattlefield() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        castPalaceJailer(player1, harness.getPermanentId(player2, "Benalish Cavalry"));

        destroyWithDismember(harness.getPermanentId(player1, "Palace Jailer"));

        harness.assertInGraveyard(player1, "Palace Jailer");
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Benalish Cavalry");

        harness.addToBattlefield(player1, new BenalishCavalry());
        castPalaceJailer(player2, harness.getPermanentId(player1, "Benalish Cavalry"));

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Becoming monarch again does not return creatures exiled by your Jailers")
    void sameControllerBecomingMonarchDoesNotEndExile() {
        harness.addToBattlefield(player2, new BenalishCavalry());
        castPalaceJailer(player1, harness.getPermanentId(player2, "Benalish Cavalry"));

        harness.addToBattlefield(player2, new BenalishCavalry());
        castPalaceJailer(player1, harness.getPermanentId(player2, "Benalish Cavalry"));

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
        harness.assertNotOnBattlefield(player2, "Benalish Cavalry");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Benalish Cavalry", "Benalish Cavalry");
    }

    private void destroyWithDismember(UUID targetId) {
        harness.setHand(player1, List.of(new Dismember()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void castPalaceJailer(com.github.laxika.magicalvibes.model.Player player, UUID targetId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new PalaceJailer()));
        addPalaceJailerMana(player);
        harness.castCreature(player, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addPalaceJailerMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
