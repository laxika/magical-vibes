package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LothlorienBlade.class, LlanowarElves.class, GrizzlyBears.class, EnormousBaloth.class})
class LothlorienBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature deals power damage to a creature defending player controls")
    void equippedCreatureDealsPowerDamageToDefendingCreature() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        elf.setPowerModifier(2);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Elf equip attaches to an Elf")
    void elfEquipAttachesToElf() {
        Permanent blade = addBladeReady(player1);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(elf.getId());
    }

    @Test
    @DisplayName("Elf equip rejects a non-Elf creature")
    void elfEquipRejectsNonElf() {
        addBladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf");
    }

    @Test
    @DisplayName("Generic equip attaches to a non-Elf creature")
    void genericEquipAttachesToNonElf() {
        Permanent blade = addBladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    private Permanent addBladeReady(Player player) {
        Permanent blade = new Permanent(new LothlorienBlade());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blade);
        return blade;
    }

    private void declareAttack(Player player, Permanent attacker) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player.getId()).indexOf(attacker);
        gs.declareAttackers(gd, player, List.of(index), null);
    }
}
