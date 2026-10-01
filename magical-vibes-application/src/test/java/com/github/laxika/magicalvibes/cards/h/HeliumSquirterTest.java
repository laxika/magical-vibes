package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EnemyOfTheGuildpact;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliumSquirter.class, EnemyOfTheGuildpact.class})
class HeliumSquirterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        Permanent squirter = castSquirter();

        assertThat(squirter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent squirter = castSquirter();

        Permanent enemy = castEnemyOfTheGuildpact(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(squirter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(enemy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may decline moving a counter onto an entering creature")
    void graftMayBeDeclined() {
        Permanent squirter = castSquirter();

        Permanent enemy = castEnemyOfTheGuildpact(player1);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(squirter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(enemy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        enemy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent squirter = castSquirter();
        squirter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(squirter),
                null, enemy.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The activated ability can target an opponent's creature and does not tap this creature")
    void targetsOpponentCreatureWithoutTappingSource() {
        Permanent enemy = addCreatureReady(player2, new EnemyOfTheGuildpact());
        enemy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent squirter = castSquirter();
        squirter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(squirter),
                null, enemy.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.FLYING)).isTrue();
        assertThat(squirter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability does nothing if its target loses its counter before resolution")
    void targetMustStillHaveCounterOnResolution() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        enemy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent squirter = castSquirter();
        squirter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(squirter),
                null, enemy.getId());
        enemy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemy, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent enemy = addCreatureReady(player1, new EnemyOfTheGuildpact());
        Permanent squirter = castSquirter();
        squirter.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(squirter), null, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castEnemyOfTheGuildpact(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new EnemyOfTheGuildpact(), "{4}{B}");
        harness.passBothPriorities();
        return findPermanent(player, "Enemy of the Guildpact");
    }

    private Permanent castSquirter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new HeliumSquirter(), "{4}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Helium Squirter");
    }
}
