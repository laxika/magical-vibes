package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({MaggotTherapy.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class})
class MaggotTherapyTest extends BaseCardTest {

    @Test
    @DisplayName("Maggot Therapy can be cast during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Maggot Therapy attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MaggotTherapy
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/-2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaggotTherapy());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost ends when Maggot Therapy leaves the battlefield")
    void effectsStopWhenRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaggotTherapy());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A 2/2 creature dies from Maggot Therapy's -2 toughness")
    void negativeToughnessCausesCreatureToDie() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> bears.getId().equals(permanent.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MaggotTherapy);
    }

    @Test
    @DisplayName("Maggot Therapy fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof MaggotTherapy);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MaggotTherapy);
    }

    @Test
    @DisplayName("Maggot Therapy cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Maggot Therapy boosts only the enchanted creature you control")
    void canEnchantOwnCreatureWithoutBoostingOthers() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent other = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flash allows casting with a spell on the stack and both Auras apply")
    void canRespondToAnotherTherapyWithFlash() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MaggotTherapy(), new MaggotTherapy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.castEnchantment(player1, 0, giant.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(giant);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof HillGiant);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof MaggotTherapy).hasSize(2);
    }
}
