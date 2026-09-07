package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({OathswornKnight.class, Shock.class})
class OathswornKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Oathsworn Knight enters with four +1/+1 counters")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new OathswornKnight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Oathsworn Knight");
        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Damage is prevented and removes one counter per damage event")
    void damageRemovesOneCounterPerEvent() {
        Permanent knight = addCreatureReady(player2, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, knight.getId());
        harness.passBothPriorities();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(knight.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oathsworn Knight");
    }

    @Test
    @DisplayName("Oathsworn Knight must attack each combat when able")
    void mustAttackWhenAble() {
        Permanent knight = addCreatureReady(player1, new OathswornKnight());
        knight.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
