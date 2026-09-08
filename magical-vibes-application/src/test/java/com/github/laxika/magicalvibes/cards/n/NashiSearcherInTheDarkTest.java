package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NashiSearcherInTheDark.class, Pacifism.class, Shock.class, YomijiWhoBarsTheWay.class})
class NashiSearcherInTheDarkTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage mills that many cards and offers any number of legendary or enchantment cards")
    void millsDamageAmountAndReturnsAcceptedEligibleCards() {
        Permanent nashi = addAttackingNashi();
        nashi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of());
        YomijiWhoBarsTheWay legendary = new YomijiWhoBarsTheWay();
        Pacifism enchantment = new Pacifism();
        Shock invalid = new Shock();
        harness.setLibrary(player1, List.of(legendary, enchantment, invalid));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(legendary, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(invalid);
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Putting no milled eligible cards into hand puts a +1/+1 counter on Nashi")
    void declinesAllEligibleCardsAndGetsCounter() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        YomijiWhoBarsTheWay legendary = new YomijiWhoBarsTheWay();
        Pacifism enchantment = new Pacifism();
        harness.setLibrary(player1, List.of(legendary, enchantment));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(legendary, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(legendary, enchantment);
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No eligible milled cards automatically put a +1/+1 counter on Nashi")
    void noEligibleCardsGetsCounter() {
        Permanent nashi = addAttackingNashi();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(nashi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addAttackingNashi() {
        Permanent nashi = addCreatureReady(player1, new NashiSearcherInTheDark());
        nashi.setAttacking(true);
        return nashi;
    }
}
