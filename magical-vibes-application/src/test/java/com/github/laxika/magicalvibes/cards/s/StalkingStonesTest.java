package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(StalkingStones.class)
class StalkingStonesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Stalking Stones produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent stones = addStones(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(stones);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability makes Stalking Stones a 3/3 artifact creature that's still a land")
    void becomesThreeThreeArtifactCreature() {
        Permanent stones = addStones(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stones)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stones)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stones)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, stones)).isTrue();
        assertThat(gqs.isLand(gd, stones)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, stones, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(stones.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Animation lasts indefinitely, surviving end-of-turn cleanup")
    void animationSurvivesCleanup() {
        Permanent stones = addStones(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stones)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stones)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, stones)).isTrue();
        assertThat(gqs.isLand(gd, stones)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, stones, CardSubtype.ELEMENTAL)).isTrue();
    }

    @Test
    @DisplayName("Stalking Stones is not a creature before the ability resolves")
    void notACreatureBeforeActivation() {
        Permanent stones = addStones(player1);

        assertThat(gqs.isCreature(gd, stones)).isFalse();
    }

    @Test
    @DisplayName("Animated Stalking Stones still taps for colorless mana")
    void animatedStillProducesColorlessMana() {
        Permanent stones = addStones(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(stones);
        gs.tapPermanent(gd, player1, index);

        assertThat(gqs.isLand(gd, stones)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addStones(Player player) {
        return addCreatureReady(player, new StalkingStones());
    }
}
