package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Nonland cards and opponents' lands do not reduce the casting cost")
    void ignoresNonlandsAndOpponentsGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Rumbleweed()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Having more than ten lands in the graveyard leaves the green cost")
    void reductionCannotRemoveColoredManaCost() {
        harness.setGraveyard(player1, IntStream.range(0, 12)
                .<Card>mapToObj(i -> new Forest()).toList());
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creatures present when the enter trigger resolves are affected, later creatures are not")
    void affectedCreaturesAreDeterminedAtResolution() {
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Other Rumbleweeds receive the boost, but lands do not")
    void excludesOnlySourceAndNoncreatures() {
        Permanent otherRumbleweed = harness.addToBattlefieldAndReturn(player1, new Rumbleweed());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Rumbleweed()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, otherRumbleweed)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, otherRumbleweed)).isEqualTo(11);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.TRAMPLE)).isFalse();
    }
}
