package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CudgelTroll;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdiousTrowTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration ability paid with black grants a regeneration shield")
    void resolvingWithBlackGrantsShield() {
        Permanent trow = addOdiousTrowReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2); // {1} + {B/G} paid black

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trow.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Hybrid {B/G} portion can be paid with green instead")
    void resolvingWithGreenGrantsShield() {
        Permanent trow = addOdiousTrowReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2); // {1} + {B/G} paid green

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trow.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addOdiousTrowReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1); // only pays part of {1}{B/G}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Odious Trow from lethal combat damage when blocking")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent trow = addOdiousTrowReady(player1);
        trow.setRegenerationShield(1);
        trow.setBlocking(true);
        trow.addBlockingTarget(0);

        // 4-power attacker deals lethal to the 1/1 Trow
        CudgelTroll attackerCard = new CudgelTroll();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent survivor = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Odious Trow"))
                .findFirst().orElseThrow();
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Odious Trow dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShield() {
        Permanent trow = addOdiousTrowReady(player1);
        trow.setBlocking(true);
        trow.addBlockingTarget(0);

        CudgelTroll attackerCard = new CudgelTroll();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Odious Trow"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Odious Trow"));
    }

    private Permanent addOdiousTrowReady(Player player) {
        OdiousTrow card = new OdiousTrow();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
