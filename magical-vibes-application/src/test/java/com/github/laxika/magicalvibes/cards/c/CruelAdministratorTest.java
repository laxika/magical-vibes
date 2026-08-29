package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        harness.setHand(player1, List.of(new CruelAdministrator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Cruel Administrator");
    }
}
