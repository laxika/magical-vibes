package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rumbleweed.class, Forest.class, GrizzlyBears.class})
class RumbleweedTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each land card in your graveyard")
    void costsOneLessForEachLandInGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB boosts and grants trample to other creatures you control")
    void enterTriggerBoostsOtherOwnCreaturesAndGrantsTrample() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rumbleweed = findPermanent(player1, "Rumbleweed");
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, rumbleweed)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, rumbleweed)).isEqualTo(8);
    }

    @Test
    @DisplayName("ETB boost and trample grant wear off at end of turn")
    void enterTriggerEffectsWearOffAtEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
    }
}
