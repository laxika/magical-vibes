package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlazingRootwalla;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoLashclaw.class, GrizzlyBears.class, BlazingRootwalla.class})
class ViashinoLashclawTest extends BaseCardTest {

    @Test
    void discardingCardGrantsHasteToCreaturesYouControlUntilEndOfTurn() {
        Permanent lashclaw = addCreatureReady(player1, new ViashinoLashclaw());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(lashclaw.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lashclaw, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lashclaw, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    @Test
    void paysTapAndDiscardCostsBeforeGrantingHaste() {
        Permanent lashclaw = addCreatureReady(player1, new ViashinoLashclaw());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of(new ViashinoLashclaw()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(lashclaw.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Viashino Lashclaw");
        harness.assertInGraveyard(player1, "Viashino Lashclaw");
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    void affectsCreaturesPresentAtResolutionButNotThoseEnteringLater() {
        addCreatureReady(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of(new ViashinoLashclaw()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new ViashinoLashclaw());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new ViashinoLashclaw());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of(new ViashinoLashclaw()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Viashino Lashclaw");
        harness.assertNotInGraveyard(player1, "Viashino Lashclaw");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent lashclaw = addCreatureReady(player1, new ViashinoLashclaw());
        lashclaw.tap();
        harness.setHand(player1, List.of(new ViashinoLashclaw()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Viashino Lashclaw");
        harness.assertNotInGraveyard(player1, "Viashino Lashclaw");
    }

    @Test
    void creatureCastForMadnessFromDiscardCostReceivesHaste() {
        addCreatureReady(player1, new ViashinoLashclaw());
        harness.setHand(player1, List.of(new BlazingRootwalla()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Blazing Rootwalla");
        harness.assertNotInGraveyard(player1, "Blazing Rootwalla");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Blazing Rootwalla"), Keyword.HASTE)).isTrue();
    }
}
