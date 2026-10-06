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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paradox resolves before the spell and does not trigger for its own cast")
    void exileCastDoublesExistingSisterhoodOnly() {
        Permanent sisterhood = castSisterhood();
        Card spell = new SisterhoodOfKarn();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent newlyEntered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(spell.getId()))
                .findFirst().orElseThrow();
        assertThat(newlyEntered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paradox doubles the counters present at resolution")
    void doublesCurrentCountersRatherThanCountersAtTriggerTime() {
        Permanent sisterhood = castSisterhood();
        Card spell = new SisterhoodOfKarn();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        sisterhood.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.passBothPriorities();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An opponent casting from exile does not trigger Paradox")
    void opponentExileCastDoesNotDoubleCounters() {
        Permanent sisterhood = castSisterhood();
        Card spell = new SisterhoodOfKarn();
        gd.addToExile(player2.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castFromExile(player2, spell.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(sisterhood.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castSisterhood() {
        harness.castFromHand(player1, new SisterhoodOfKarn(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Sisterhood of Karn");
    }
}
