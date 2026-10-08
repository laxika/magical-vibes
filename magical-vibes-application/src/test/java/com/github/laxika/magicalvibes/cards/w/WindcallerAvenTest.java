package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindcallerAven.class, MotherBear.class, SnowCoveredIsland.class})
class WindcallerAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling queues a target creature choice and draws a card")
    void cyclingQueuesTargetChoiceAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);
        harness.assertInGraveyard(player1, "Windcaller Aven");
        harness.assertInHand(player1, "Mother Bear");
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    void cyclingWithoutALegalTargetStillDraws() {
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mother Bear");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingCannotTargetAPlayer() {
        harness.addToBattlefield(player1, new MotherBear());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cyclingCanGrantFlyingToAnOpponentsCreatureBeforeDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MotherBear());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Windcaller Aven");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        harness.assertInHand(player1, "Mother Bear");
    }

    @Test
    void cyclingCannotTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredIsland());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MotherBear());
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
        harness.assertInHand(player1, "Mother Bear");
    }

    @Test
    void cyclingRequiresBlueManaAndDoesNotDiscardWhenPaymentFails() {
        harness.setHand(player1, List.of(new WindcallerAven()));
        harness.setLibrary(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Windcaller Aven");
        harness.assertNotInGraveyard(player1, "Windcaller Aven");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
