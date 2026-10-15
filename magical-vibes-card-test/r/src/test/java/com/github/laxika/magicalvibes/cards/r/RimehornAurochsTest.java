package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorealCentaur;
import com.github.laxika.magicalvibes.cards.b.BullAurochs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimehornAurochs.class, BullAurochs.class, BorealCentaur.class})
class RimehornAurochsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with other Aurochs gives +1/+0 for each one")
    void boostsForEachOtherAttackingAurochs() {
        Permanent rimehorn = addCreatureReady(player1, new RimehornAurochs());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BorealCentaur());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(rimehorn.getPowerModifier()).isEqualTo(2);
        assertThat(rimehorn.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Nonattacking Aurochs do not increase the attack trigger")
    void ignoresNonAttackingAurochs() {
        Permanent rimehorn = addCreatureReady(player1, new RimehornAurochs());
        addCreatureReady(player1, new BullAurochs());
        addCreatureReady(player1, new BorealCentaur());

        declareAttackers(List.of(0, 2));
        resolveAllTriggers();

        assertThat(rimehorn.getPowerModifier()).isZero();
        assertThat(rimehorn.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Snow ability makes the first target block the second target if able")
    void makesTargetCreatureBlockTargetCreature() {
        addCreatureReady(player1, new RimehornAurochs());
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("A tapped blocker is not required to block because it cannot block")
    void tappedBlockerIsNotRequiredToBlock() {
        addCreatureReady(player1, new RimehornAurochs());
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        blocker.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(blocker.getId(), attacker.getId()));
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("The same creature can be chosen for both target positions")
    void allowsSameCreatureForBothTargets() {
        Permanent rimehorn = addCreatureReady(player1, new RimehornAurochs());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(rimehorn.getId(), rimehorn.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonsnow mana cannot pay the snow portion of the activation cost")
    void requiresSnowMana() {
        addCreatureReady(player1, new RimehornAurochs());
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(blocker.getId(), attacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No blocking requirement is created when the second target leaves before resolution")
    void doesNothingWhenSecondTargetLeaves() {
        addCreatureReady(player1, new RimehornAurochs());
        Permanent attacker = addCreatureReady(player1, new BorealCentaur());
        Permanent blocker = addCreatureReady(player2, new BorealCentaur());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(blocker.getId(), attacker.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack bonus counts other Aurochs still attacking when the trigger resolves")
    void countsAttackingAurochsAtResolution() {
        Permanent rimehorn = addCreatureReady(player1, new RimehornAurochs());
        Permanent bull = addCreatureReady(player1, new BullAurochs());

        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(bull);
        gd.playerGraveyards.get(player1.getId()).add(bull.getCard());
        resolveAllTriggers();

        assertThat(rimehorn.getPowerModifier()).isZero();
        assertThat(rimehorn.getToughnessModifier()).isZero();
    }
}
