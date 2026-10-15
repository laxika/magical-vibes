package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakheartDryads.class, GoldenHind.class, FontOfFertility.class})
class OakheartDryadsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry gives a target creature +1/+1 until end of turn")
    void ownEntryBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        castOakheartDryads(player1, bears);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Another enchantment entering under your control gives a target creature +1/+1")
    void allyEnchantmentEntryBoostsTargetCreature() {
        harness.addToBattlefield(player1, new OakheartDryads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player1, List.of(new FontOfFertility()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-enchantment creature entering does not trigger it")
    void creatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OakheartDryads());
        harness.setHand(player1, List.of(new GoldenHind()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OakheartDryads());
        harness.setHand(player2, List.of(new FontOfFertility()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        castOakheartDryads(player1, bears);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getEffectivePower()).isEqualTo(3);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Its entry cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new FontOfFertility());
        harness.setHand(player1, List.of(new OakheartDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Its own entry can target itself and triggers only once")
    void ownEntryCanTargetItself() {
        harness.setHand(player1, List.of(new OakheartDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dryads = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, dryads.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(dryads.getEffectivePower()).isEqualTo(3);
        assertThat(dryads.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Oakheart Dryads triggers both its own and the existing Dryads' ability")
    void enchantmentCreatureEntryTriggersBothDryads() {
        harness.addToBattlefield(player1, new OakheartDryads());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        castOakheartDryads(player1, target);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private void castOakheartDryads(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new OakheartDryads()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, 0, target.getId());
    }
}
