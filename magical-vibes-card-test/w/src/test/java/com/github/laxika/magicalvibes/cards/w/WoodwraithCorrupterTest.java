package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodwraithCorrupter.class, Forest.class, Mountain.class})
class WoodwraithCorrupterTest extends BaseCardTest {

    @Test
    @DisplayName("Target Forest becomes a permanent 4/4 black and green Elemental Horror")
    void targetForestBecomesPermanentAnimation() {
        Permanent corrupter = addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(corrupter.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.ELEMENTAL, CardSubtype.HORROR);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    @DisplayName("Permanent Forest animation survives cleanup")
    void animationSurvivesCleanup() {
        addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        forest.resetModifiers();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.ELEMENTAL, CardSubtype.HORROR);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Forest land")
    void cannotTargetNonForest() {
        addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Forest");
    }

    @Test
    @DisplayName("Can target a Forest controlled by an opponent")
    void canTargetOpponentsForest() {
        addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
    }

    @Test
    @DisplayName("Animation resolves after its source leaves and survives a turn boundary")
    void animationSurvivesSourceLeavingAndTurnBoundary() {
        Permanent corrupter = addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, corrupter));
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, forest))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.ELEMENTAL, CardSubtype.HORROR);
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    @DisplayName("Animated Forest retains its green mana ability")
    void animatedForestRetainsManaAbility() {
        addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = addCreatureReady(player1, new Forest());
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        harness.tapPermanent(player1, 1);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(greenBefore + 1);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent corrupter = harness.addToBattlefieldAndReturn(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(corrupter.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Activation requires black mana as well as generic and green mana")
    void cannotActivateWithoutBlackMana() {
        Permanent corrupter = addCreatureReady(player1, new WoodwraithCorrupter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(corrupter.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
