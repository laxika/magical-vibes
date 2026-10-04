package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoldRush.class, GrizzlyBears.class})
class GoldRushTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure and gives the target creature +2/+2")
    void createsTreasureAndBoostsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGoldRush(target);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts the Treasure it creates for the target creature's boost")
    void countsNewTreasureForBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldRush(), new GoldRush()));
        addMana(2);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("The creature boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGoldRush(target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new GoldRush()));
        addMana(1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates a Treasure without choosing a target or having any creatures")
    void createsTreasureWithoutTarget() {
        harness.setHand(player1, List.of(new GoldRush()));
        addMana(1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Gold Rush");
    }

    @Test
    @DisplayName("Creates no Treasure if the chosen creature leaves before resolution")
    void illegalTargetPreventsTreasureCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldRush()));
        addMana(1);
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Gold Rush");
    }

    @Test
    @DisplayName("Counts only the spell controller's Treasures when boosting an opposing creature")
    void ignoresOpponentsTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GoldRush()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);

        castGoldRush(target);

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("The resolved boost stays fixed when another Treasure is created")
    void boostDoesNotChangeAfterResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldRush(), new GoldRush()));
        addMana(2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts Treasures created in response before the targeted spell resolves")
    void countsTreasuresAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GoldRush(), new GoldRush()));
        addMana(2);
        harness.castInstant(player1, 0, target.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    private void castGoldRush(Permanent target) {
        harness.setHand(player1, List.of(new GoldRush()));
        addMana(1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.GREEN, amount);
        harness.addMana(player1, ManaColor.COLORLESS, amount);
    }
}
