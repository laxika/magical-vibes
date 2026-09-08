package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TurnaboutTest extends BaseCardTest {

    @Test
    @DisplayName("Taps only untapped creatures controlled by the target player")
    void tapsCreatures() {
        Permanent untappedCreature = addPermanent(player2, new GrizzlyBears());
        Permanent tappedCreature = addPermanent(player2, new GrizzlyBears());
        tappedCreature.tap();
        Permanent artifact = addPermanent(player2, new JayemdaeTome());
        Permanent land = addPermanent(player2, new Forest());

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "TAP_CREATURE");

        assertThat(untappedCreature.isTapped()).isTrue();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps only tapped lands controlled by the target player")
    void untapsLands() {
        Permanent tappedLand = addPermanent(player2, new Forest());
        tappedLand.tap();
        Permanent untappedLand = addPermanent(player2, new Forest());
        Permanent creature = addPermanent(player2, new GrizzlyBears());
        creature.tap();

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "UNTAP_LAND");

        assertThat(tappedLand.isTapped()).isFalse();
        assertThat(untappedLand.isTapped()).isFalse();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can choose artifacts and does not affect permanents of the other player")
    void tapsArtifactsOnlyOnTargetPlayer() {
        Permanent artifact = addPermanent(player2, new JayemdaeTome());
        Permanent creature = addPermanent(player2, new GrizzlyBears());
        Permanent ownArtifact = addPermanent(player1, new JayemdaeTome());

        castTurnabout(player2.getId());
        harness.handleListChoice(player1, "TAP_ARTIFACT");

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(ownArtifact.isTapped()).isFalse();
    }

    private Permanent addPermanent(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private void castTurnabout(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Turnabout()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
