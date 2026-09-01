package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MesaFalcon.class, BeastWalkers.class})
class MesaFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Mesa Falcon")
    void resolvingAbilityBoostsToughness() {
        Permanent falcon = addCreatureReady(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, falcon)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(2);
        assertThat(falcon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated multiple times, boosts stack")
    void canActivateMultipleTimes() {
        Permanent falcon = addCreatureReady(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent falcon = addCreatureReady(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability requires one white mana and one generic mana")
    void abilityRequiresWhiteAndGenericMana() {
        Permanent falcon = addCreatureReady(player1, new MesaFalcon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-tap ability can be activated while Mesa Falcon has summoning sickness")
    void canActivateWhileSummoningSick() {
        Permanent falcon = addCreatureReady(player1, new MesaFalcon());
        falcon.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, falcon)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Mesa Falcon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new MesaFalcon());
        addCreatureReady(player2, new BeastWalkers());

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
