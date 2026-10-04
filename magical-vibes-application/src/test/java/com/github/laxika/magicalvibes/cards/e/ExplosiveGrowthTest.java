package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplosiveGrowth.class, NomadicElf.class, ChromaticSphere.class})
class ExplosiveGrowthTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusTwoPlusTwoWithoutKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        Permanent resolvedBear = findPermanent(player1, "Nomadic Elf");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(4);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void givesTargetCreaturePlusFivePlusFiveWithKicker() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player1, "Nomadic Elf");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(7);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void canTargetCreatureAnOpponentControls() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        Permanent resolvedBear = findPermanent(player2, "Nomadic Elf");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(4);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent resolvedBear = findPermanent(player1, "Nomadic Elf");
        assertThat(resolvedBear.getEffectivePower()).isEqualTo(2);
        assertThat(resolvedBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new NomadicElf());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new ChromaticSphere());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void kickedBoostWearsOffAtEndOfTurn() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedInstant(player1, 0, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getEffectivePower()).isEqualTo(7);
        assertThat(elf.getEffectiveToughness()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent resolvedElf = findPermanent(player1, "Nomadic Elf");
        assertThat(resolvedElf.getEffectivePower()).isEqualTo(2);
        assertThat(resolvedElf.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void kickedAndUnkickedSpellsStackTheirBoosts() {
        Permanent elf = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new ExplosiveGrowth(), new ExplosiveGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, elf.getId());
        harness.castKickedInstant(player1, 0, elf.getId());
        harness.passBothPriorities();

        Permanent resolvedElf = findPermanent(player2, "Nomadic Elf");
        assertThat(resolvedElf.getEffectivePower()).isEqualTo(9);
        assertThat(resolvedElf.getEffectiveToughness()).isEqualTo(9);
        harness.assertInGraveyard(player1, "Explosive Growth");
    }
}
