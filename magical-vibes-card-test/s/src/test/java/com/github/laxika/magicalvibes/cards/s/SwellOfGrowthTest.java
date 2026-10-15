package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({SwellOfGrowth.class, Forest.class, GrizzlyBears.class, Unsummon.class})
class SwellOfGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature and may put a land from hand onto the battlefield")
    void boostsAndPutsLandFromHandOntoBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SwellOfGrowth(), forest));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent enteredForest = findPermanent(player1, "Forest");
        assertThat(enteredForest.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the land choice leaves the land in hand")
    void decliningLandChoiceLeavesLandInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SwellOfGrowth(), forest));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(forest.getId()));
    }

    @Test
    @DisplayName("The land choice offers only land cards and the boost expires at cleanup")
    void filtersLandChoiceAndExpiresBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SwellOfGrowth(), bears, forest));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice =
                (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SwellOfGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The creature is boosted even when there are no lands in hand")
    void boostsWithoutLandInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new SwellOfGrowth(), bears));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Swell of Growth");
    }

    @Test
    @DisplayName("Puts only one land onto the battlefield on the opponent's turn without using a land play")
    void putsOneLandOnOpponentsTurnAfterLandPlayUsed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest chosen = new Forest();
        Forest remaining = new Forest();
        harness.forceActivePlayer(player2);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new SwellOfGrowth(), chosen, remaining));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("An illegal creature target prevents the land effect as well")
    void doesNotPutLandWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SwellOfGrowth(), forest));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Swell of Growth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
