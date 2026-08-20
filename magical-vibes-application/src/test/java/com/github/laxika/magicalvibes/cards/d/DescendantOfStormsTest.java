package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DescendantOfStormsTest extends BaseCardTest {

    private static final String COUNTERS = "Put 1 +1/+1 counter on this permanent";
    private static final String SPIRIT = "Create a 1/1 white Spirit creature token";

    @Test
    void payingManaCanPutACounterOnDescendant() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, COUNTERS);

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void payingManaCanCreateASpirit() {
        addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent descendant = addCreatureReady(player1, new DescendantOfStorms());
        addManaForEndure();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void addManaForEndure() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
