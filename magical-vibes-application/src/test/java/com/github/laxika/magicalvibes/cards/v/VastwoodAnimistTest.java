package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VastwoodAnimist.class, Forest.class})
class VastwoodAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Animating a land makes it an X/X Elemental creature that is still a land")
    void animatesLandAsElemental() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(1);
        assertThat(forest.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Animation scales with the number of Allies controlled")
    void animationScalesWithAllyCount() {
        addReadyAnimist(player1);
        addAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player1);

        activateAnimist(forest);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getTransientSubtypes()).doesNotContain(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Cannot target a land controlled by an opponent")
    void cannotTargetOpponentsLand() {
        addReadyAnimist(player1);
        Permanent forest = addForest(player2);

        assertThatThrownBy(() -> activateAnimist(forest))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsAlliesAtResolutionAndKeepsThatSize() {
        Permanent animist = addReadyAnimist(player1);
        Permanent forest = addForest(player1);
        addAnimist(player2);

        harness.activateAbility(player1, 0, null, forest.getId());
        assertThat(animist.isTapped()).isTrue();
        Permanent additionalAlly = addAnimist(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).remove(additionalAlly);
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(2);
    }

    @Test
    void resolvesAfterSourceLeavesAndZeroAlliesKillsLand() {
        Permanent animist = addReadyAnimist(player1);
        Permanent forest = addForest(player1);
        harness.activateAbility(player1, 0, null, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(animist);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        addAnimist(player1);
        Permanent forest = addForest(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent animist = addReadyAnimist(player1);
        Permanent forest = addForest(player1);
        animist.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonland() {
        Permanent animist = addReadyAnimist(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, animist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAnimist(Player player) {
        return addCreatureReady(player, new VastwoodAnimist());
    }

    private Permanent addAnimist(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VastwoodAnimist());
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void activateAnimist(Permanent forest) {
        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
    }
}
