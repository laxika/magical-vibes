package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.InevitableDefeat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FortressKinGuard.class, InevitableDefeat.class})
class FortressKinGuardTest extends BaseCardTest {

    private static final String COUNTERS = "Put 1 +1/+1 counter on this permanent";
    private static final String SPIRIT = "Create a 1/1 white Spirit creature token";

    @Test
    void enteringCanPutACounterOnFortressKinGuard() {
        harness.castFromHand(player1, new FortressKinGuard(), "{1}{W}");
        harness.passBothPriorities();
        Permanent fortressKinGuard = findPermanent(player1, "Fortress Kin-Guard");

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTERS);

        assertThat(fortressKinGuard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void enteringCanCreateASpirit() {
        harness.castFromHand(player1, new FortressKinGuard(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, SPIRIT);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void createsASpiritWhenExiledBeforeEndureResolves() {
        harness.castFromHand(player1, new FortressKinGuard(), "{1}{W}");
        harness.passBothPriorities();
        Permanent guard = findPermanent(player1, "Fortress Kin-Guard");

        harness.setHand(player2, List.of(new InevitableDefeat()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, guard.getId());
        harness.assertNotOnBattlefield(player1, "Fortress Kin-Guard");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
