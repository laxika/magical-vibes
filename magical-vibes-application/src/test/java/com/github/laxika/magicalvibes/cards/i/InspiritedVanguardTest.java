package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiritedVanguard.class, InevitableDefeat.class})
class InspiritedVanguardTest extends BaseCardTest {

    private static final String COUNTERS = "Put 2 +1/+1 counters on this permanent";
    private static final String SPIRIT = "Create a 2/2 white Spirit creature token";

    @Test
    void enteringCanPutCountersOnInspiritedVanguard() {
        Permanent vanguard = castVanguard();

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void enteringCanCreateASpirit() {
        castVanguard();

        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void attackingCanEndure() {
        Permanent vanguard = addCreatureReady(player1, new InspiritedVanguard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackingCanCreateASpiritInsteadOfCounters() {
        Permanent vanguard = addCreatureReady(player1, new InspiritedVanguard());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(spirit.isTapped()).isFalse();
        assertThat(spirit.isAttacking()).isFalse();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void createsASpiritWhenExiledBeforeEnteringTriggerResolves() {
        Permanent vanguard = castVanguard();

        harness.setHand(player2, List.of(new InevitableDefeat()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, vanguard.getId());
        harness.assertNotOnBattlefield(player1, "Inspirited Vanguard");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    private Permanent castVanguard() {
        harness.castFromHand(player1, new InspiritedVanguard(), "{4}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Inspirited Vanguard");
    }
}
