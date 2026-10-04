package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hydrosurge.class, WalkingCorpse.class, JayemdaeTome.class})
class HydrosurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-0 until end of turn")
    void givesMinusFivePower() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not kill the creature since toughness is unchanged")
    void doesNotKillCreature() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new WalkingCorpse());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can reduce the power of a creature you control below zero")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Two copies cumulatively reduce only the chosen creature's power")
    void multipleCopiesStackWithoutAffectingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new Hydrosurge(), new Hydrosurge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-8);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }
}
