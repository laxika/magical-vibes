package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UniversalSurveillance.class, GrizzlyBears.class, WurmsTooth.class})
class UniversalSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws X cards")
    void drawsXCards() {
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Universal Surveillance");
    }

    @Test
    @DisplayName("Improvise lets an artifact pay the generic mana")
    void improvisePaysGenericMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X zero draws no cards")
    void zeroXDrawsNoCards() {
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.setLibrary(player1, List.of(new UniversalSurveillance()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Universal Surveillance");
    }

    @Test
    @DisplayName("Improvise pays all of X without reducing the number of cards drawn")
    void improvisePaysAllOfX() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.setLibrary(player1, List.of(
                new UniversalSurveillance(), new UniversalSurveillance(), new UniversalSurveillance()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        gs.playCard(gd, player1, 0, 2, null, null, List.of(),
                List.of(firstArtifact.getId(), secondArtifact.getId()));
        harness.passBothPriorities();

        assertThat(firstArtifact.isTapped()).isTrue();
        assertThat(secondArtifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Universal Surveillance");
    }

    @Test
    @DisplayName("Improvise cannot pay a blue mana requirement")
    void improviseCannotPayBlueMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null, List.of(),
                List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An already tapped artifact cannot pay for improvise")
    void tappedArtifactCannotPayForImprovise() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        artifact.tap();
        harness.setHand(player1, List.of(new UniversalSurveillance()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null, List.of(),
                List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
