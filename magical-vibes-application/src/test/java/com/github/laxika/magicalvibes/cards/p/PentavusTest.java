package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PentavusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five +1/+1 counters, making it a 5/5")
    void entersWithFiveCounters() {
        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, pentavus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, pentavus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter creates a 1/1 flying Pentavite token")
    void removeCounterCreatesPentavite() {
        Permanent pentavus = addCreatureReady(player1, new Pentavus());
        pentavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Pentavite");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pentavus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot make a token with no +1/+1 counters left")
    void cannotMakeTokenWithoutCounters() {
        addCreatureReady(player1, new Pentavus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a Pentavite puts a +1/+1 counter back on Pentavus")
    void sacrificingPentaviteAddsCounter() {
        Permanent pentavus = addCreatureReady(player1, new Pentavus());
        pentavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pentavite");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability with no Pentavite to sacrifice")
    void cannotSacrificeWithoutPentavite() {
        Permanent pentavus = addCreatureReady(player1, new Pentavus());
        pentavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
