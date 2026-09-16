package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SerraTheBenevolent.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class SerraTheBenevolentTest extends BaseCardTest {

    @Test
    @DisplayName("+2 boosts only controlled creatures with flying until end of turn")
    void plusTwoBoostsControlledFlyingCreatures() {
        Permanent serra = addReadySerra(4);
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(serra), 0, null, null);
        harness.passBothPriorities();

        assertThat(serra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownAngel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownAngel)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingAngel)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownAngel)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 creates one Angel token")
    void minusThreeCreatesAngelToken() {
        Permanent serra = addReadySerra(4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(serra), 1, null, null);
        harness.passBothPriorities();

        assertThat(serra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("-6 emblem keeps a creature controller's life total at 1 against damage")
    void minusSixEmblemPreventsLethalDamageWhileControllingCreature() {
        Permanent serra = addReadySerra(6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(serra), 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    private Permanent addReadySerra(int loyalty) {
        Permanent perm = new Permanent(new SerraTheBenevolent());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
