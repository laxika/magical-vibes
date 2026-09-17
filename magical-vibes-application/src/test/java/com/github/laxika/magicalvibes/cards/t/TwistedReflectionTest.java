package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwistedReflection.class, GrizzlyBears.class, HornedTurtle.class, DuskdaleWurm.class})
class TwistedReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives a creature -6/-0 until end of turn")
    void reducesTargetCreaturePower() {
        Permanent bears = addCreature(new GrizzlyBears());

        cast(new int[]{0}, List.of(bears.getId()), false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second mode switches a creature's power and toughness until end of turn")
    void switchesTargetCreaturePowerAndToughness() {
        Permanent turtle = addCreature(new HornedTurtle());

        cast(new int[]{1}, List.of(turtle.getId()), false);

        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entwine pays {B} and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent bears = addCreature(new GrizzlyBears());
        Permanent turtle = addCreature(new HornedTurtle());

        cast(new int[]{0, 1}, List.of(bears.getId(), turtle.getId()), true);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-4);
        assertThat(gqs.getEffectivePower(gd, turtle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, turtle)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwined modes may share the same target")
    void entwinedModesMayShareTarget() {
        Permanent wurm = addCreature(new DuskdaleWurm());

        cast(new int[]{0, 1}, List.of(wurm.getId(), wurm.getId()), true);

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entwine without the additional black mana is rejected")
    void entwineRequiresAdditionalMana() {
        Permanent bears = addCreature(new GrizzlyBears());
        Permanent turtle = addCreature(new HornedTurtle());
        harness.setHand(player1, List.of(new TwistedReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(bears.getId(), turtle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes reject a player target")
    void modesRequireCreatureTargets() {
        harness.setHand(player1, List.of(new TwistedReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Card creature) {
        return harness.addToBattlefieldAndReturn(player2, creature);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new TwistedReflection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (entwined) {
            harness.addMana(player1, ManaColor.BLACK, 1);
        }
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
