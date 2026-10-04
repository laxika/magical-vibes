package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlailingManticore.class})
class FlailingManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay to give the Manticore +1/+1 until end of turn")
    void anyPlayerMayBoostManticore() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Any player may pay to give the Manticore -1/-1 until end of turn")
    void anyPlayerMayShrinkManticore() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(-1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Power and toughness modifiers wear off at end of turn")
    void modifiersWearOffAtEndOfTurn() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(manticore.getPowerModifier()).isEqualTo(1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple activations of the same ability stack")
    void multipleActivationsStack() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(2);
        assertThat(manticore.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A negative modifier expires at cleanup")
    void negativeModifierExpires() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(manticore.getPowerModifier()).isEqualTo(-1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent can shrink the Manticore to zero toughness")
    void opponentCanShrinkManticoreToDeath() {
        FlailingManticore card = new FlailingManticore();
        Permanent manticore = addCreatureReady(player1, card);
        harness.addMana(player2, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player2, 0, 1, null, null);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(manticore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("The activating opponent must pay even when the controller has mana")
    void opponentCannotSpendControllersMana() {
        Permanent manticore = addCreatureReady(player1, new FlailingManticore());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesDoNotRequireTappingOrHaste() {
        Permanent manticore = harness.addToBattlefieldAndReturn(player1, new FlailingManticore());
        manticore.setSummoningSick(true);
        manticore.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(manticore.getPowerModifier()).isEqualTo(1);
        assertThat(manticore.getToughnessModifier()).isEqualTo(1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(manticore.getPowerModifier()).isZero();
        assertThat(manticore.getToughnessModifier()).isZero();
        assertThat(manticore.isTapped()).isTrue();
    }
}
