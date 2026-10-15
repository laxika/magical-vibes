package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Earthbrawn.class, ElvishWarrior.class, MurmuringBosk.class})
class EarthbrawnTest extends BaseCardTest {

    @Test
    @DisplayName("Cast as an instant gives target creature +3/+3")
    void spellBoostsTargetCreature() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new Earthbrawn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(warrior.getEffectivePower()).isEqualTo(5);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Spell boost wears off at cleanup")
    void spellBoostWearsOff() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new Earthbrawn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warrior.getEffectivePower()).isEqualTo(2);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Reinforce 1 puts a +1/+1 counter on target creature")
    void reinforcePutsCounter() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(warrior.getEffectivePower()).isEqualTo(3);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Earthbrawn");
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature; no cost is paid")
    void reinforceRejectsNonCreature() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Earthbrawn");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The instant cannot target a non-creature; no cost is paid")
    void spellRejectsNonCreature() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Earthbrawn");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reinforce can target a creature an opponent controls")
    void reinforceCanTargetOpponentsCreature() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Earthbrawn");
    }

    @Test
    @DisplayName("Reinforce pays mana and discards before its counter resolves")
    void reinforcePaysCostsBeforeResolution() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, warrior.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Earthbrawn");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reinforce counter survives end-of-turn cleanup")
    void reinforceCounterSurvivesCleanup() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, warrior.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(warrior.getEffectivePower()).isEqualTo(3);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Reinforce cannot be activated without green mana and does not discard")
    void reinforceRequiresGreenMana() {
        harness.setHand(player1, List.of(new Earthbrawn()));
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Earthbrawn");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
