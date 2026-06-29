package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkirsdagCultistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with only self as creature auto-sacrifices self and puts ability on stack")
    void autoSacrificesSelfAsOnlyCreature() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Auto-sacrificed the only creature (itself)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Skirsdag Cultist"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple creatures asks to choose which to sacrifice")
    void asksForChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a creature to sacrifice puts ability on stack")
    void choosingCreaturePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);
        UUID elvesId = findPermanent(player1, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, elvesId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Cultist should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Skirsdag Cultist"));
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player on resolution")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);
        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Cannot activate ability without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        // Remove the cultist so there are no creatures
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new SkirsdagCultist());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without red mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SkirsdagCultist());

        Permanent cultist = findPermanent(player1, "Skirsdag Cultist");
        cultist.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
