package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarmonyOfNature.class, BearCub.class})
class HarmonyOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping all creatures gains 4 life for each")
    void tapsAllCreaturesGainsFourEach() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new BearCub());

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
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new BearCub());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Only the caster's creatures can be tapped")
    void onlyCasterCreaturesCanBeTapped() {
        harness.setLife(player1, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BearCub());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Tapping no creatures gains no life")
    void tapsNoneGainsNoLife() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new BearCub());

        castHarmonyOfNature();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(a.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolves harmlessly with no untapped creatures")
    void noUntappedCreaturesResolvesHarmlessly() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new BearCub());
        tapped.tap();

        castHarmonyOfNature();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    private void castHarmonyOfNature() {
        harness.castFromHand(player1, new HarmonyOfNature(), "{2}{G}");
        harness.passBothPriorities();
    }
}
