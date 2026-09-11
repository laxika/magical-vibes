package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshalingTheTroops.class, ForestBear.class, Forest.class})
class MarshalingTheTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping all creatures gains 4 life for each")
    void tapsAllCreaturesGainsFourEach() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new ForestBear());

        castMarshalingTheTroops();
        harness.handleMultiplePermanentsChosen(player1, List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("Tapping a subset gains life only for the creatures tapped")
    void tapsSubsetGainsForTappedOnly() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new ForestBear());

        castMarshalingTheTroops();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapped.getId()));

        assertThat(tapped.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Tapping no creatures gains no life")
    void tapsNoneGainsNoLife() {
        harness.setLife(player1, 20);
        Permanent a = harness.addToBattlefieldAndReturn(player1, new ForestBear());

        castMarshalingTheTroops();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(a.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Resolves harmlessly with no untapped creatures")
    void noUntappedCreaturesResolvesHarmlessly() {
        harness.setLife(player1, 20);
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        tapped.tap();

        castMarshalingTheTroops();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can choose only untapped creatures controlled by the caster")
    void onlyCasterCreaturesAreEligible() {
        harness.setLife(player1, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ForestBear());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ForestBear());

        castMarshalingTheTroops();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ownCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    private void castMarshalingTheTroops() {
        harness.setHand(player1, List.of(new MarshalingTheTroops()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
