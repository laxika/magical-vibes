package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Triskelavus.class})
class TriskelavusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters, making it a 4/4")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new Triskelavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent triskelavus = findPermanent(player1, "Triskelavus");
        assertThat(triskelavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, triskelavus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, triskelavus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a flying Triskelavite token")
    void removeCounterCreatesToken() {
        Permanent triskelavus = addCreatureReady(player1, new Triskelavus());
        triskelavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(triskelavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Triskelavite");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A Triskelavite can sacrifice itself to deal 1 damage to a player")
    void tokenSacrificesToDealDamage() {
        Permanent triskelavus = addCreatureReady(player1, new Triskelavus());
        triskelavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Triskelavite");
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.setLife(player2, 20);
        harness.activateAbility(player1, tokenIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Triskelavite");
    }

    @Test
    @DisplayName("Cannot remove a +1/+1 counter when none remain")
    void cannotRemoveCounterWithoutCounters() {
        addCreatureReady(player1, new Triskelavus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
