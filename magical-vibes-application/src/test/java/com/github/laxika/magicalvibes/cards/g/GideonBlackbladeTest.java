package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SorceressQueen;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonBlackblade.class, FountainOfYouth.class, GrizzlyBears.class, Shock.class,
        SorceressQueen.class, Swamp.class})
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

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {"VIGILANCE", "LIFELINK", "INDESTRUCTIBLE"})
    @DisplayName("Each +1 choice grants only the chosen keyword and expires at end of turn")
    void chosenKeywordExpiresAtEndOfTurn(Keyword keyword) {
        addReadyGideon(player1, 4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        String label = keyword.name().charAt(0) + keyword.name().substring(1).toLowerCase();
        harness.handleListChoice(player1, label);

        for (Keyword candidate : List.of(Keyword.VIGILANCE, Keyword.LIFELINK, Keyword.INDESTRUCTIBLE)) {
            assertThat(gqs.hasKeyword(gd, bear, candidate)).isEqualTo(candidate == keyword);
        }

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, bear, keyword)).isFalse();
    }

    @Test
    @DisplayName("+1 does not resolve when its chosen creature dies in response")
    void plusOneWithRemovedTargetDoesNotResolve() {
        Permanent gideon = addReadyGideon(player1, 4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        castShock(player2, bear);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-6 cannot target a land")
    void minusSixRejectsLand() {
        Permanent gideon = addReadyGideon(player1, 6);
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gideon can exile himself when he retains loyalty after paying -6")
    void minusSixCanExileSelf() {
        Permanent gideon = addReadyGideon(player1, 7);

        harness.activateAbility(player1, 0, 1, null, gideon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gideon);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gideon.getCard());
    }

    @Test
    @DisplayName("A later base power and toughness effect overrides Gideon's 4/4 animation")
    void laterBasePowerToughnessOverridesAnimation() {
        Permanent gideon = addReadyGideon(player1, 4);
        addCreatureReady(player2, new SorceressQueen());

        harness.activateAbility(player2, 0, 0, null, gideon.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(2);
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
        harness.castAndResolveInstant(caster, 0, target.getId());
    }
}
