package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TolarianAcademy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenHerd.class, Island.class, TolarianAcademy.class, Disenchant.class})
class HiddenHerdTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's nonbasic land play makes Hidden Herd a 3/3 Beast creature")
    void becomesBeastCreatureWhenOpponentPlaysNonbasicLand() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new TolarianAcademy()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenHerd)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenHerd)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenHerd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hiddenHerd)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenHerd)).containsExactly(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("A basic land play does not trigger Hidden Herd")
    void doesNotTriggerForBasicLand() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new Island()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenHerd)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenHerd)).isFalse();
    }

    @Test
    @DisplayName("A controller's nonbasic land play does not trigger Hidden Herd")
    void doesNotTriggerForControllerLand() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TolarianAcademy()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenHerd)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenHerd)).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land entering without being played does not trigger Hidden Herd")
    void doesNotTriggerWhenLandEntersWithoutBeingPlayed() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());

        harness.addToBattlefield(player2, new TolarianAcademy());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, hiddenHerd)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenHerd)).isFalse();
    }

    @Test
    @DisplayName("Hidden Herd does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new TolarianAcademy()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenHerd)).isTrue();

        gd.landsPlayedThisTurn.put(player2.getId(), 0);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TolarianAcademy()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, hiddenHerd)).isTrue();
    }

    @Test
    @DisplayName("Destroying Hidden Herd in response prevents its pending animation")
    void doesNotAnimateAfterLeavingBattlefield() {
        Permanent hiddenHerd = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new TolarianAcademy(), new Disenchant()));

        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, hiddenHerd.getId());

        harness.assertNotOnBattlefield(player1, "Hidden Herd");
        harness.assertInGraveyard(player1, "Hidden Herd");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Hidden Herd");
        harness.assertInGraveyard(player1, "Hidden Herd");
    }

    @Test
    @DisplayName("Each Hidden Herd animates independently from the same nonbasic land play")
    void animatesEachCopyIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HiddenHerd());
        prepareOpponentLandPlay();
        harness.setHand(player2, List.of(new TolarianAcademy()));

        harness.playLand(player2, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        for (Permanent herd : List.of(first, second)) {
            assertThat(gqs.isCreature(gd, herd)).isTrue();
            assertThat(gqs.isEnchantment(gd, herd)).isFalse();
            assertThat(gqs.getEffectivePower(gd, herd)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, herd)).isEqualTo(3);
        }
    }

    private void prepareOpponentLandPlay() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
