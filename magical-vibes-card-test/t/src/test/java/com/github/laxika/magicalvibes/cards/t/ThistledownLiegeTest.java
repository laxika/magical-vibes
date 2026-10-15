package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThistledownLiege.class, EliteVanguard.class, FugitiveWizard.class, GrizzlyBears.class})
class ThistledownLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other white creatures you control")
    void buffsOtherWhite() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        Permanent white = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());

        // 2/1 base + 1/1 = 3/2
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs other blue creatures you control")
    void buffsOtherBlue() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        // 1/1 base + 1/1 = 2/2
        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new ThistledownLiege());

        // Base 1/3, unaffected by its own "other" boosts
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff creatures that are neither white nor blue")
    void doesNotBuffOffColor() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's white creatures")
    void doesNotBuffOpponent() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        Permanent opponentWhite = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, opponentWhite)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentWhite)).isEqualTo(1);
    }

    @Test
    @DisplayName("A white-and-blue creature gets both boosts")
    void whiteAndBlueGetsBothBoosts() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        harness.addToBattlefield(player1, new ThistledownLiege());

        // The second Liege is both white and blue, so it receives +1/+1 twice from the first.
        Permanent boosted = findPermanent(player1, "Thistledown Liege");

        // Base 1/3 + 1/1 (white) + 1/1 (blue) = 3/5
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE"})
    @DisplayName("Flash allows casting during an opponent's upkeep with either hybrid color")
    void canFlashInDuringOpponentsUpkeep(ManaColor color) {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new ThistledownLiege());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new ThistledownLiege()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, color, 3);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Thistledown Liege")).isEqualTo(2);
        for (Permanent liege : findPermanents(player1, "Thistledown Liege")) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("Multiple Lieges stack their boosts and stop boosting when they leave")
    void boostsStackAndEndWhenSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThistledownLiege());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThistledownLiege());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ThistledownLiege());

        for (Permanent liege : List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(7);
        }

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, third));

        assertThat(countPermanents(player1, "Thistledown Liege")).isEqualTo(2);
        for (Permanent liege : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(5);
        }
    }

    @Test
    @DisplayName("Does not boost an opponent's white-and-blue creature")
    void doesNotBoostOpponentsMulticoloredCreature() {
        harness.addToBattlefield(player1, new ThistledownLiege());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ThistledownLiege());

        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(3);
    }
}
