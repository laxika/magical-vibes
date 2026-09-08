package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({RhoxPummeler.class, Shock.class})
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
        harness.castInstant(player1, 0, pummeler.getId());
        harness.passBothPriorities();

        assertThat(pummeler.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gqs.hasKeyword(gd, pummeler, Keyword.TRAMPLE)).isFalse();
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
