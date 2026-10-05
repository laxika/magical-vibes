package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MireShade.class, Forest.class, Swamp.class})
class MireShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice is paid immediately and a summoning-sick Shade gets its counter on resolution")
    void sacrificeIsCostAndDoesNotRequireHaste() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new MireShade());
        shade.setSummoningSick(true);
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.setTapped(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(swamp);
        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate again while an earlier activation is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent shade = addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent remainingSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(remainingSwamp);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("{B}, sacrificing a Swamp puts a +1/+1 counter on it")
    void sacrificeSwampAddsCounter() {
        Permanent shade = addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("With multiple Swamps the controller chooses which one to sacrifice")
    void promptsForSwampChoice() {
        Permanent shade = addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(shade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(second);
    }

    @Test
    @DisplayName("Cannot be activated with only a non-Swamp land to sacrifice")
    void requiresSwamp() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated without black mana")
    void requiresBlackMana() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot use an opponent's Swamp as the sacrifice cost")
    void requiresControllerSwamp() {
        addCreatureReady(player1, new MireShade());
        Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentSwamp);
    }

    @Test
    @DisplayName("Cannot be activated during an opponent's main phase")
    void cannotActivateOnOpponentsTurn() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated outside a main phase")
    void cannotActivateAtInstantSpeed() {
        addCreatureReady(player1, new MireShade());
        harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
