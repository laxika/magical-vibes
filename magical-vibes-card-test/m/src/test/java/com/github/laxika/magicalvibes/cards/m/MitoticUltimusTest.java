package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MitoticUltimus.class, MitoticSlime.class, GrizzlyBears.class, WrathOfGod.class})
class MitoticUltimusTest extends BaseCardTest {

    @Test
    void costsLessByGreatestControlledCreaturePower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MitoticUltimus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotUseOpponentsCreaturePowerForTheReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MitoticUltimus()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathTriggerConjuresTwoRealMitoticSlimes() {
        harness.addToBattlefield(player1, new MitoticUltimus());
        harness.castFromHand(player1, new WrathOfGod(), "{W}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> slimes = findPermanents(player1, "Mitotic Slime");
        assertThat(slimes).hasSize(2);
        assertThat(slimes).allSatisfy(slime -> assertThat(slime.getCard().isToken()).isFalse());
    }

    @Test
    void usesGreatestPowerRatherThanTotalPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MitoticSlime());

        harness.castFromHand(player1, new MitoticUltimus(), "{3}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void stillRequiresBothGreenManaWhenPowerExceedsGenericCost() {
        harness.addToBattlefield(player1, new MitoticUltimus());
        harness.setHand(player1, List.of(new MitoticUltimus()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void paysFullCostWithoutControlledCreatures() {
        harness.castFromHand(player1, new MitoticUltimus(), "{7}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void deathTriggerConjuresForTheDyingCreaturesController() {
        harness.addToBattlefield(player2, new MitoticUltimus());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mitotic Slime")).isEmpty();
        assertThat(findPermanents(player2, "Mitotic Slime")).hasSize(2)
                .allSatisfy(slime -> {
                    assertThat(slime.getCard().isToken()).isFalse();
                    assertThat(slime.getCard().getOwnerId()).isEqualTo(player2.getId());
                });
    }
}
