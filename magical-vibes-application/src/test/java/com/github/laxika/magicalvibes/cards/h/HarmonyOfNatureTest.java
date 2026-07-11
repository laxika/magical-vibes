package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarmonyOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping all creatures gains 4 life for each")
    void tapsAllCreaturesGainsFourEach() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Tapping a subset gains life only for the creatures tapped")
    void tapsSubsetGainsForTappedOnly() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Tapping no creatures gains no life")
    void tapsNoneGainsNoLife() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(a.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolves harmlessly with no untapped creatures")
    void noUntappedCreaturesResolvesHarmlessly() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tapped.tap();

        castHarmonyOfNature();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    private void castHarmonyOfNature() {
        harness.setHand(player1, List.of(new HarmonyOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
