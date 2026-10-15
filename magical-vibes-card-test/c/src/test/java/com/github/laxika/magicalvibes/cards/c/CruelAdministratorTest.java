package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CruelAdministrator.class)
class CruelAdministratorTest extends BaseCardTest {

    @Test
    void entersWithoutRaidWithoutCounter() {
        Permanent administrator = castAdministrator(false);

        assertThat(administrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithRaidWithCounter() {
        Permanent administrator = castAdministrator(true);

        assertThat(administrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        Permanent administrator = castAdministrator(false);

        assertThat(administrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void soldierFirebendingProducesManaThatLastsUntilCombatEnds() {
        Permanent administrator = addCreatureReady(player1, new CruelAdministrator());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(administrator)));
        resolveAllTriggers();
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.isTapped()).isFalse();
        assertThat(soldier.isAttacking()).isFalse();
        advanceToUpkeep(player1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(soldier)));
            resolveAllTriggers();
        });

        assertThat(gd.playerManaPools.get(player1.getId()).getColoredManaTotals()
                .getOrDefault(ManaColor.RED, 0)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).getColoredManaTotals()
                .getOrDefault(ManaColor.RED, 0)).isEqualTo(1);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).getColoredManaTotals()
                .getOrDefault(ManaColor.RED, 0)).isZero();
    }

    @Test
    void attackingCreatesSoldierWithFirebending() {
        Permanent administrator = addCreatureReady(player1, new CruelAdministrator());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(administrator)));
        resolveAllTriggers();

        Permanent soldier = findPermanents(player1, "Soldier").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(soldier.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(soldier.getEffectivePower()).isEqualTo(2);
        assertThat(soldier.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.FIREBENDING)).isTrue();
    }

    private Permanent castAdministrator(boolean raid) {
        if (raid) {
            gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        }
        harness.castFromHand(player1, new CruelAdministrator(), "{3}{B}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Cruel Administrator");
    }
}
