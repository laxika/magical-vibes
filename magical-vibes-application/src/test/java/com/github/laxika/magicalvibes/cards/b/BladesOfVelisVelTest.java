package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinKing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BladesOfVelisVelTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Both target creatures get +2/+0")
    void twoTargetsGetBoost() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castInstant(player1, 0, List.of(a.getId(), b.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, a)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(2);
    }

    @Test
    @DisplayName("Targets gain all creature types, so Goblin King buffs a non-Goblin")
    void grantsAllCreatureTypes() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // not a Goblin yet

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        // 2 base +2 (Blades) +1 (Goblin King, now a Goblin via Changeling)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        // toughness 2 +0 (Blades) +1 (Goblin King)
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void singleTargetAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost and creature types wear off at end of turn")
    void wearsOff() {
        harness.addToBattlefield(player1, new GoblinKing());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        // Simulate end-of-turn cleanup: the changeling grant is a floating CR 613 layer-4/6
        // effect that expires with the until-end-of-turn floating effects, alongside the
        // Permanent-level modifier reset.
        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2); // no boost, no longer a Goblin
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
