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

    @Test
    @DisplayName("A newly cast, tapped Triskelavus can spend all three counters")
    void canUseAllCountersWhileSummoningSickAndTapped() {
        harness.setHand(player1, List.of(new Triskelavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent triskelavus = findPermanent(player1, "Triskelavus");
        triskelavus.tap();
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            assertThat(triskelavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2 - i);
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Triskelavite")).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, triskelavus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, triskelavus)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Triskelavus");
    }

    @Test
    @DisplayName("Creating a token requires mana and does not remove a counter when payment fails")
    void cannotCreateTokenWithoutMana() {
        Permanent triskelavus = addCreatureReady(player1, new Triskelavus());
        triskelavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(triskelavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Triskelavite");
    }

    @Test
    @DisplayName("A new Triskelavite can sacrifice itself immediately to deal lethal damage to a creature")
    void tokenCanDealLethalDamageToCreature() {
        Permanent triskelavus = addCreatureReady(player1, new Triskelavus());
        triskelavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addCreatureReady(player2, new Triskelavus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, target.getId());
        harness.assertNotOnBattlefield(player1, "Triskelavite");
        harness.assertOnBattlefield(player2, "Triskelavus");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Triskelavus");
        harness.assertInGraveyard(player2, "Triskelavus");
    }

    @Test
    @DisplayName("A Triskelavite may target itself but is sacrificed before its ability resolves")
    void tokenCanTargetItself() {
        Permanent triskelavus = addCreatureReady(player1, new Triskelavus());
        triskelavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Triskelavite");
        harness.activateAbility(player1, 1, null, token.getId());
        harness.assertNotOnBattlefield(player1, "Triskelavite");
        harness.passBothPriorities();

        assertThat(triskelavus.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
