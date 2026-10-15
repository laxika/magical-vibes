package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KitchenImp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CapturedByLagacs.class, KitchenImp.class})
class CapturedByLagacsTest extends BaseCardTest {

    @Test
    @DisplayName("Support 2 puts a +1/+1 counter on each of up to two target creatures")
    void supportsTwoCreaturesWhenItEnters() {
        Permanent enchanted = addCreatureReady(player1, new KitchenImp());
        Permanent firstTarget = addCreatureReady(player1, new KitchenImp());
        Permanent secondTarget = addCreatureReady(player2, new KitchenImp());

        harness.setHand(player1, List.of(new CapturedByLagacs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof CapturedByLagacs
                        && enchanted.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("The enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player1, new KitchenImp());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CapturedByLagacs());
        aura.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(enchanted))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player2, new KitchenImp());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void supportCanChooseNoCreatures() {
        Permanent enchanted = addCreatureReady(player2, new KitchenImp());
        castAura(enchanted);

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Captured by Lagacs");
    }

    @Test
    void supportCanChooseOnlyTheEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new KitchenImp());
        Permanent unchosen = addCreatureReady(player1, new KitchenImp());
        castAura(enchanted);

        harness.handlePermanentChosen(player1, enchanted.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void supportCannotChooseTheSameCreatureTwice() {
        Permanent enchanted = addCreatureReady(player2, new KitchenImp());
        Permanent target = addCreatureReady(player1, new KitchenImp());
        castAura(enchanted);

        harness.handlePermanentChosen(player1, target.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotChooseSupportTargetsWhileCastingTheAura() {
        Permanent enchanted = addCreatureReady(player2, new KitchenImp());
        Permanent supportTarget = addCreatureReady(player1, new KitchenImp());
        harness.setHand(player1, List.of(new CapturedByLagacs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(enchanted.getId(), supportTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAura(Permanent enchanted) {
        harness.setHand(player1, List.of(new CapturedByLagacs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();
    }
}
