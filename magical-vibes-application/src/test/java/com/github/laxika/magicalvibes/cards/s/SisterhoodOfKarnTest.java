package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SisterhoodOfKarn.class, GrizzlyBears.class})
class SisterhoodOfKarnTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent sisterhood = castSisterhood();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sisterhood.getEffectivePower()).isEqualTo(1);
        assertThat(sisterhood.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paradox doubles its +1/+1 counters when casting from exile")
    void paradoxDoublesCountersFromExile() {
        Permanent sisterhood = castSisterhood();
        Card spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        Permanent sisterhood = castSisterhood();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castSisterhood() {
        harness.setHand(player1, List.of(new SisterhoodOfKarn()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Sisterhood of Karn");
    }
}
