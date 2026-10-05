package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HerdchaserDragon;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuringAether.class, HerdchaserDragon.class, HillGiant.class})
class ObscuringAetherTest extends BaseCardTest {

    @Test
    void reducesFaceDownCreatureSpellCost() {
        harness.addToBattlefield(player1, new ObscuringAether());
        harness.setHand(player1, List.of(new HerdchaserDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotReduceFaceUpCreatureSpellCost() {
        harness.addToBattlefield(player1, new ObscuringAether());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void turnsItselfFaceDownAsA2By2Creature() {
        Permanent aether = harness.addToBattlefieldAndReturn(player1, new ObscuringAether());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(aether.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, aether)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aether)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void multipleAethersReduceFaceDownCastingCostToZero() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new ObscuringAether());
        }
        harness.setHand(player1, List.of(new HerdchaserDragon()));

        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsAetherDoesNotReduceYourCastingCost() {
        harness.addToBattlefield(player2, new ObscuringAether());
        harness.setHand(player1, List.of(new HerdchaserDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownAetherStopsReducingCastingCosts() {
        Permanent aether = harness.addToBattlefieldAndReturn(player1, new ObscuringAether());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new HerdchaserDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.isCreature(gd, aether)).isTrue();
        assertThat(gqs.isEnchantment(gd, aether)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void faceDownAetherCannotTurnItselfFaceUpOrActivateItsPrintedAbility() {
        Permanent aether = harness.addToBattlefieldAndReturn(player1, new ObscuringAether());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(aether.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceTheCostOfTurningAMorphFaceUp() {
        harness.addToBattlefield(player1, new ObscuringAether());
        harness.setHand(player1, List.of(new HerdchaserDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, 1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
