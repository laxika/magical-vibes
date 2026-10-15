package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DestroyTheEvidence.class, Forest.class, AxebaneGuardian.class})
class DestroyTheEvidenceTest extends BaseCardTest {

    private void castAt(UUID targetId) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new DestroyTheEvidence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Destroys the targeted land and mills its controller up to and including the first land revealed")
    void destroysLandAndMillsUntilLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setLibrary(player2, List.of(
                new AxebaneGuardian(),
                new AxebaneGuardian(),
                new Forest(),      // first land -> stop
                new AxebaneGuardian() // stays in library
        ));

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Forest", "Axebane Guardian", "Axebane Guardian", "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Axebane Guardian");
    }

    @Test
    @DisplayName("A landless library is entirely milled")
    void millsEntireLibraryWhenNoLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setLibrary(player2, List.of(new AxebaneGuardian(), new AxebaneGuardian()));

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Forest", "Axebane Guardian", "Axebane Guardian");
    }

    @Test
    @DisplayName("The controller's own land is a legal target and mills the controller")
    void millsOwnControllerWhenTargetingOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setLibrary(player1, List.of(new AxebaneGuardian(), new Forest()));

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name")
                .contains("Axebane Guardian", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AxebaneGuardian());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DestroyTheEvidence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The destroyed land enters the graveyard before the revealed cards")
    void destroysLandBeforePuttingRevealedCardsInGraveyard() {
        Forest destroyedLand = new Forest();
        Permanent land = harness.addToBattlefieldAndReturn(player2, destroyedLand);
        Forest revealedLand = new Forest();
        harness.setLibrary(player2, List.of(revealedLand));

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(destroyedLand, revealedLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the land from being destroyed")
    void destroysLandWithEmptyLibrary() {
        Forest destroyedLand = new Forest();
        Permanent land = harness.addToBattlefieldAndReturn(player2, destroyedLand);
        harness.setLibrary(player2, List.of());

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(destroyedLand);
    }

    @Test
    @DisplayName("No cards are revealed when the targeted land leaves before resolution")
    void doesNotMillWhenTargetLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        AxebaneGuardian creature = new AxebaneGuardian();
        Forest libraryLand = new Forest();
        harness.setLibrary(player2, List.of(creature, libraryLand));

        castAt(land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerHands.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature, libraryLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land controlled by another player mills its controller rather than its owner")
    void millsControllerOfLandOwnedByAnotherPlayer() {
        Forest ownedLand = new Forest();
        ownedLand.setOwnerId(player1.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player2, ownedLand);
        Forest controllerLibraryLand = new Forest();
        AxebaneGuardian ownerLibraryCard = new AxebaneGuardian();
        harness.setLibrary(player2, List.of(controllerLibraryLand));
        harness.setLibrary(player1, List.of(ownerLibraryCard));

        castAt(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownedLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(controllerLibraryLand);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownerLibraryCard);
    }
}
