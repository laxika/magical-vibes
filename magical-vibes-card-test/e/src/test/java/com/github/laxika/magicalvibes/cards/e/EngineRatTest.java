package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({EngineRat.class})
class EngineRatTest extends BaseCardTest {

    @Test
    @DisplayName("{5}{B}: each opponent loses 2 life")
    void abilityMakesOpponentLoseLife() {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new EngineRat());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(rat.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires {5}{B}")
    void abilityRequiresMana() {
        harness.addToBattlefieldAndReturn(player1, new EngineRat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Rat can activate repeatedly")
    void tappedRatCanActivateRepeatedly() {
        Permanent rat = harness.addToBattlefieldAndReturn(player1, new EngineRat());
        rat.setSummoningSick(true);
        rat.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(rat.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Generic mana cannot replace the black activation cost")
    void abilityRequiresBlackMana() {
        harness.addToBattlefield(player1, new EngineRat());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deathtouch destroys a creature with more toughness than damage dealt")
    void deathtouchKillsLargerCreature() {
        addCreatureReady(player2, new EngineRat());
        Permanent attacker = addCreatureReady(player1, new EngineRat());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Engine Rat");
        harness.assertNotOnBattlefield(player2, "Engine Rat");
        harness.assertInGraveyard(player1, "Engine Rat");
        harness.assertInGraveyard(player2, "Engine Rat");
    }
}
