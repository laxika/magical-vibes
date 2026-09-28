package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HearthKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PullUnder.class, HearthKami.class, KamiOfOldStone.class, Island.class})
class PullUnderTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -5/-5 and dies when toughness drops to 0 or less")
    void killsSmallCreature() {
        Permanent target = addCreature(player2);
        castPullUnder(target);

        harness.assertNotOnBattlefield(player2, "Hearth Kami");
        harness.assertInGraveyard(player2, "Hearth Kami");
    }

    @Test
    @DisplayName("A surviving creature's -5/-5 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addBigCreature(player2);
        castPullUnder(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        Permanent own = addCreature(player1);
        castPullUnder(own);

        harness.assertNotOnBattlefield(player1, "Hearth Kami");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Island());
        Permanent island = findPermanent(player2, "Island");
        harness.setHand(player1, List.of(new PullUnder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castPullUnder(Permanent target) {
        harness.setHand(player1, List.of(new PullUnder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new HearthKami());
    }

    private Permanent addBigCreature(Player player) {
        return addCreatureReady(player, new KamiOfOldStone());
    }
}
