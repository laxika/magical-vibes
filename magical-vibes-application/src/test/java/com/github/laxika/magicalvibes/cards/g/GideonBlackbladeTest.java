package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonBlackblade.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class})
class GideonBlackbladeTest extends BaseCardTest {

    @Test
    @DisplayName("During its controller's turn Gideon is a 4/4 indestructible creature")
    void animatesDuringControllerTurn() {
        Permanent gideon = addReadyGideon(player1, 4);

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Gideon prevents damage to itself only during its controller's turn")
    void preventsDamageDuringControllerTurnOnly() {
        Permanent gideon = addReadyGideon(player1, 4);

        castShock(player1, gideon);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castShock(player2, gideon);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 grants the chosen keyword to up to one other creature you control")
    void plusOneGrantsChosenKeyword() {
        Permanent gideon = addReadyGideon(player1, 4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may choose no target and cannot target Gideon or an opponent's creature")
    void plusOneTargeting() {
        Permanent gideon = addReadyGideon(player1, 4);
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, gideon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-6 exiles a target nonland permanent")
    void minusSixExilesNonlandPermanent() {
        addReadyGideon(player1, 6);
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.activateAbility(player1, 0, 1, null, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fountain);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fountain.getCard());
    }

    private Permanent addReadyGideon(Player player, int loyalty) {
        Permanent gideon = harness.addToBattlefieldAndReturn(player, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, loyalty);
        gideon.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gideon;
    }

    private void castShock(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
