package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronLadDivergingDestiny.class, Forest.class, Ornithopter.class})
class IronLadDivergingDestinyTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability draws the revealed artifact card")
    void drawsRevealedArtifactCard() {
        Permanent ironLad = addReadyIronLad();
        Ornithopter artifact = new Ornithopter();
        Forest nextCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact, nextCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(ironLad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability does not draw when the revealed card is not an artifact")
    void doesNotDrawRevealedNonartifactCard() {
        Permanent ironLad = addReadyIronLad();
        Forest nonartifact = new Forest();
        Ornithopter nextCard = new Ornithopter();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonartifact, nextCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonartifact, nextCard);
        assertThat(ironLad.isTapped()).isTrue();
    }

    private Permanent addReadyIronLad() {
        Permanent ironLad = harness.addToBattlefieldAndReturn(player1, new IronLadDivergingDestiny());
        ironLad.setSummoningSick(false);
        return ironLad;
    }
}
