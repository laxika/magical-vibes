package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegalImperiosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dinosaurs you control get +1/+1")
    void buffsOtherDinosaursYouControl() {
        harness.addToBattlefield(player1, new RaptorCompanion());

        Permanent dinosaur = findPermanent(player1, "Raptor Companion");
        int powerBefore = gqs.getEffectivePower(gd, dinosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, dinosaur);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new RegalImperiosaur());

        Permanent imperiosaur = findPermanent(player1, "Regal Imperiosaur");
        int powerBefore = gqs.getEffectivePower(gd, imperiosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, imperiosaur);

        harness.addToBattlefield(player1, new RegalImperiosaur());
        Permanent secondImperiosaur = findPermanents(player1, "Regal Imperiosaur").getLast();

        assertThat(gqs.getEffectivePower(gd, imperiosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, imperiosaur)).isEqualTo(toughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, secondImperiosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, secondImperiosaur)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost non-Dinosaurs")
    void doesNotBoostNonDinosaurs() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        int powerBefore = gqs.getEffectivePower(gd, bears);
        int toughnessBefore = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost an opponent's Dinosaurs")
    void doesNotBoostOpponentDinosaurs() {
        harness.addToBattlefield(player2, new RaptorCompanion());

        Permanent opponentDinosaur = findPermanent(player2, "Raptor Companion");
        int powerBefore = gqs.getEffectivePower(gd, opponentDinosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, opponentDinosaur);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, opponentDinosaur)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opponentDinosaur)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("The bonus is removed when Regal Imperiosaur leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new RegalImperiosaur());

        Permanent dinosaur = findPermanent(player1, "Raptor Companion");
        int powerWithBonus = gqs.getEffectivePower(gd, dinosaur);
        int toughnessWithBonus = gqs.getEffectiveToughness(gd, dinosaur);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Regal Imperiosaur"));

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(powerWithBonus - 1);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(toughnessWithBonus - 1);
    }
}
