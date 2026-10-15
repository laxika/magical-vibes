package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Strangle;
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

@CardUsed({RhoxPummeler.class, Shock.class, Murder.class, Strangle.class})
class RhoxPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter and has trample")
    void entersWithShieldCounterAndHasTrample() {
        Permanent pummeler = castPummeler();

        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Loses trample when its shield counter is removed")
    void losesTrampleWhenShieldCounterIsRemoved() {
        Permanent pummeler = castPummeler();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Shield replaces destruction, then the unshielded creature can be destroyed")
    void shieldReplacesDestructionOnlyOnce() {
        Permanent pummeler = castPummeler();
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        harness.assertOnBattlefield(player1, "Rhox Pummeler");
        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isFalse();

        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        harness.assertNotOnBattlefield(player1, "Rhox Pummeler");
        harness.assertInGraveyard(player1, "Rhox Pummeler");
    }

    @Test
    @DisplayName("Retains trample when destruction consumes only one of multiple shields")
    void retainsTrampleWithRemainingShield() {
        Permanent pummeler = castPummeler();
        pummeler.setCounterCount(CounterType.SHIELD, 2);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, pummeler.getId());

        harness.assertOnBattlefield(player1, "Rhox Pummeler");
        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Other counters do not grant trample, but a restored shield does")
    void trampleDependsOnShieldCountersSpecifically() {
        Permanent pummeler = castPummeler();
        pummeler.setCounterCount(CounterType.SHIELD, 0);
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isFalse();

        pummeler.setCounterCount(CounterType.SHIELD, 1);

        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A shield prevents lethal damage, but a second damage event kills")
    void shieldPreventsLethalDamageOnlyOnce() {
        Permanent pummeler = castPummeler();
        harness.setHand(player1, List.of(new Strangle(), new Strangle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, pummeler.getId());

        harness.assertOnBattlefield(player1, "Rhox Pummeler");
        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isFalse();

        harness.castAndResolveSorcery(player1, 0, pummeler.getId());

        harness.assertNotOnBattlefield(player1, "Rhox Pummeler");
        harness.assertInGraveyard(player1, "Rhox Pummeler");
    }

    private Permanent castPummeler() {
        harness.setHand(player1, List.of(new RhoxPummeler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Rhox Pummeler");
    }
}
