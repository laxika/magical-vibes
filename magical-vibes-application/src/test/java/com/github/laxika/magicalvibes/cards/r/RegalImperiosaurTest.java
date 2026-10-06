package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.cards.t.TyroxSauridTyrant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalImperiosaur.class, TyroxSauridTyrant.class, JibbirikOmnivore.class})
class RegalImperiosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dinosaurs you control get +1/+1")
    void buffsOtherDinosaursYouControl() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TyroxSauridTyrant());
        int powerBefore = gqs.getEffectivePower(gd, dinosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, dinosaur);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost itself")
    void doesNotBoostItself() {
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player1, new RegalImperiosaur());
        int powerBefore = gqs.getEffectivePower(gd, imperiosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, imperiosaur);

        Permanent secondImperiosaur = harness.addToBattlefieldAndReturn(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, imperiosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, imperiosaur)).isEqualTo(toughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, secondImperiosaur)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, secondImperiosaur)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost non-Dinosaurs")
    void doesNotBoostNonDinosaurs() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JibbirikOmnivore());
        int powerBefore = gqs.getEffectivePower(gd, bears);
        int toughnessBefore = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("Regal Imperiosaur does not boost an opponent's Dinosaurs")
    void doesNotBoostOpponentDinosaurs() {
        Permanent opponentDinosaur = harness.addToBattlefieldAndReturn(player2, new TyroxSauridTyrant());
        int powerBefore = gqs.getEffectivePower(gd, opponentDinosaur);
        int toughnessBefore = gqs.getEffectiveToughness(gd, opponentDinosaur);

        harness.addToBattlefield(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, opponentDinosaur)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, opponentDinosaur)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("The bonus is removed when Regal Imperiosaur leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TyroxSauridTyrant());
        harness.addToBattlefield(player1, new RegalImperiosaur());
        int powerWithBonus = gqs.getEffectivePower(gd, dinosaur);
        int toughnessWithBonus = gqs.getEffectiveToughness(gd, dinosaur);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Regal Imperiosaur"));

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(powerWithBonus - 1);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(toughnessWithBonus - 1);
    }

    @Test
    @DisplayName("A lone Regal Imperiosaur receives no bonus from its own ability")
    void loneImperiosaurDoesNotBoostItself() {
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player1, new RegalImperiosaur());

        assertThat(gqs.getEffectivePower(gd, imperiosaur)).isEqualTo(imperiosaur.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, imperiosaur)).isEqualTo(imperiosaur.getCard().getToughness());
    }

    @Test
    @DisplayName("A Dinosaur entering after Regal Imperiosaur immediately receives the bonus")
    void boostsDinosaursEnteringLater() {
        harness.addToBattlefield(player1, new RegalImperiosaur());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new TyroxSauridTyrant());

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(dinosaur.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(dinosaur.getCard().getToughness() + 1);
    }
}
