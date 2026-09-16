package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
    void plusTwoBoostsFlyingCreatures() {
        Permanent serra = addReadySerra(4);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbility(player1, indexOf(serra), 0, null, null);
        harness.passBothPriorities();

        assertThat(serra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingAngel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 creates a 4/4 Angel token with flying and vigilance")
    void minusThreeCreatesAngelToken() {
        Permanent serra = addReadySerra(4);

        harness.activateAbility(player1, indexOf(serra), 1, null, null);
        harness.passBothPriorities();

        assertThat(serra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.ANGEL)
                        && permanent.getCard().getPower() == 4
                        && permanent.getCard().getToughness() == 4
                        && gqs.hasKeyword(gd, permanent, Keyword.FLYING)
                        && gqs.hasKeyword(gd, permanent, Keyword.VIGILANCE));
    }

    @Test
    @DisplayName("-6 emblem keeps life at 1 when its controller controls a creature")
    void minusSixEmblemAppliesConditionalDamageFloor() {
        Permanent serra = addReadySerra(6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 1);

        harness.activateAbility(player1, indexOf(serra), 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("-6 emblem does not apply without a creature")
    void minusSixEmblemRequiresAControlledCreature() {
        Permanent serra = addReadySerra(6);
        harness.setLife(player1, 1);

        harness.activateAbility(player1, indexOf(serra), 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadySerra(int loyalty) {
        Permanent serra = new Permanent(new SerraTheBenevolent());
        serra.setCounterCount(CounterType.LOYALTY, loyalty);
        serra.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serra);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return serra;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
