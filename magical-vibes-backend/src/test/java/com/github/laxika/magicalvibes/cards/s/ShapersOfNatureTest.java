package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShapersOfNatureTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has two activated abilities with correct effects")
    void hasCorrectAbilities() {
        ShapersOfNature card = new ShapersOfNature();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {3}{G}: Put a +1/+1 counter on target creature.
        var counterAbility = card.getActivatedAbilities().get(0);
        assertThat(counterAbility.isRequiresTap()).isFalse();
        assertThat(counterAbility.getManaCost()).isEqualTo("{3}{G}");
        assertThat(counterAbility.getEffects()).singleElement()
                .isInstanceOf(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class);

        // Ability 1: {2}{U}, Remove a +1/+1 counter from a creature you control: Draw a card.
        var drawAbility = card.getActivatedAbilities().get(1);
        assertThat(drawAbility.isRequiresTap()).isFalse();
        assertThat(drawAbility.getManaCost()).isEqualTo("{2}{U}");
        assertThat(drawAbility.getEffects()).hasSize(2);
        assertThat(drawAbility.getEffects().get(0)).isInstanceOf(RemoveCounterFromControlledCreatureCost.class);
        RemoveCounterFromControlledCreatureCost removeCost =
                (RemoveCounterFromControlledCreatureCost) drawAbility.getEffects().get(0);
        assertThat(removeCost.count()).isEqualTo(1);
        assertThat(removeCost.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        assertThat(drawAbility.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Ability 0: Put a +1/+1 counter on target creature =====

    @Test
    @DisplayName("Ability 0 puts a +1/+1 counter on target creature")
    void ability0PutsCounterOnTargetCreature() {
        Permanent shapers = addReadyShapers(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 0 can target opponent's creature")
    void ability0CanTargetOpponentCreature() {
        Permanent shapers = addReadyShapers(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 0 cannot be activated without enough mana")
    void ability0RequiresMana() {
        addReadyShapers(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        // Only {1}{G} — need {3}{G}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Ability 1: Remove a +1/+1 counter from a creature you control, draw a card =====

    @Test
    @DisplayName("Ability 1 auto-removes counter when only one creature has +1/+1 counters and draws a card")
    void ability1AutoRemovesCounterAndDraws() {
        Permanent shapers = addReadyShapers(player1);
        shapers.setPlusOnePlusOneCounters(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(shapers.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Ability 1 can remove counter from a different creature you control")
    void ability1CanRemoveCounterFromOtherCreature() {
        Permanent shapers = addReadyShapers(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Only bears has +1/+1 counter, so auto-select
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability 1 prompts for choice when multiple creatures have +1/+1 counters")
    void ability1PromptsWhenMultipleCreaturesHaveCounters() {
        Permanent shapers = addReadyShapers(player1);
        shapers.setPlusOnePlusOneCounters(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Ability 1 resolves after choosing creature when prompted")
    void ability1ResolvesAfterChoosingCreature() {
        Permanent shapers = addReadyShapers(player1);
        shapers.setPlusOnePlusOneCounters(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(shapers.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Ability 1 cannot be activated when no creature has +1/+1 counters")
    void ability1CannotActivateWithoutCounters() {
        addReadyShapers(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Ability 1 cannot be activated without enough mana")
    void ability1RequiresMana() {
        Permanent shapers = addReadyShapers(player1);
        shapers.setPlusOnePlusOneCounters(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        // Only {U} — need {2}{U}

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Both abilities together =====

    @Test
    @DisplayName("Can use ability 0 to add counter then ability 1 to remove it and draw")
    void canAddCounterThenRemoveItToDraw() {
        Permanent shapers = addReadyShapers(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Use ability 0 to put a +1/+1 counter on Shapers of Nature
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, shapers.getId());
        harness.passBothPriorities();

        assertThat(shapers.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Use ability 1 to remove the counter and draw a card
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(shapers.getPlusOnePlusOneCounters()).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    // ===== Helpers =====

    private Permanent addReadyShapers(Player player) {
        ShapersOfNature card = new ShapersOfNature();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
