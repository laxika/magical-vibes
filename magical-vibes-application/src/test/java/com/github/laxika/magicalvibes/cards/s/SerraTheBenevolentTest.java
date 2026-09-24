package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SerraTheBenevolent.class, AirElemental.class, GrizzlyBears.class, Shock.class})
class SerraTheBenevolentTest extends BaseCardTest {

    @Test
    @DisplayName("+2 boosts only controlled creatures with flying until end of turn")
    void plusTwoBoostsControlledFlyingCreatures() {
        addReadySerra(player1, 4);
        Permanent flyer = addReadyCreature(player1, new AirElemental());
        Permanent groundCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opposingFlyer = addReadyCreature(player2, new AirElemental());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, groundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 creates a 4/4 white Angel with flying and vigilance")
    void minusThreeCreatesAngelToken() {
        Permanent serra = addReadySerra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Angel");
        assertThat(serra.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("-6 emblem keeps life at 1 while its controller controls a creature")
    void minusSixEmblemProtectsLifeTotal() {
        addReadySerra(player1, 6);
        addReadyCreature(player1, new GrizzlyBears());
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    private Permanent addReadySerra(Player player, int loyalty) {
        Permanent permanent = new Permanent(new SerraTheBenevolent());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
