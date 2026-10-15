package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmashingSuccess.class, Forest.class, GrizzlyBears.class, Ornithopter.class, DarksteelCitadel.class})
class SmashingSuccessTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and creates a Treasure")
    void destroysArtifactAndCreatesTreasure() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castSmashingSuccess(thopter.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys target land without creating a Treasure")
    void destroysLandWithoutCreatingTreasure() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castSmashingSuccess(forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is neither an artifact nor a land")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SmashingSuccess()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An indestructible artifact land survives and creates no Treasure")
    void indestructibleArtifactCreatesNoTreasure() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());

        castSmashingSuccess(citadel.getId());

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Regeneration prevents destruction and Treasure creation")
    void regeneratedArtifactCreatesNoTreasure() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        thopter.setRegenerationShield(1);

        castSmashingSuccess(thopter.getId());

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(thopter.isTapped()).isTrue();
        assertThat(thopter.getRegenerationShield()).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own artifact and create Treasure")
    void destroysOwnArtifactAndCreatesTreasure() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castSmashingSuccess(thopter.getId());

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("A target destroyed in response creates Treasure only for the resolving spell")
    void missingTargetCreatesNoAdditionalTreasure() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new SmashingSuccess()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, thopter.getId());
        harness.setHand(player2, List.of(new SmashingSuccess()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, thopter.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    private void castSmashingSuccess(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SmashingSuccess()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
