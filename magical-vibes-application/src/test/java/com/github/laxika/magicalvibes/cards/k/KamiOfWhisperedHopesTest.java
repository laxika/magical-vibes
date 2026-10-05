package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AfiyaGrove;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfWhisperedHopes.class, AfiyaGrove.class})
class KamiOfWhisperedHopesTest extends BaseCardTest {

    @Test
    @DisplayName("Adds mana equal to its power in the chosen color")
    void addsManaEqualToPower() {
        Permanent kami = addCreatureReady(player1, new KamiOfWhisperedHopes());
        kami.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Adds a +1/+1 counter to a noncreature permanent entering under its controller's control")
    void addsCounterToNoncreaturePermanent() {
        addCreatureReady(player1, new KamiOfWhisperedHopes());

        harness.castFromHand(player1, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        Permanent grove = findPermanent(player1, "Afiya Grove");
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void multipleKamisEachAddOneCounterToTheBatch() {
        addCreatureReady(player1, new KamiOfWhisperedHopes());
        addCreatureReady(player1, new KamiOfWhisperedHopes());

        harness.castFromHand(player1, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Afiya Grove")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void doesNotIncreaseCountersOnOpponentsPermanent() {
        addCreatureReady(player1, new KamiOfWhisperedHopes());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Afiya Grove")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void increasesCounterMovedOntoItselfAndUsesTheNewPowerForMana() {
        Permanent kami = addCreatureReady(player1, new KamiOfWhisperedHopes());
        harness.castFromHand(player1, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();
        Permanent grove = findPermanent(player1, "Afiya Grove");

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, kami.getId());
        resolveAllTriggers();

        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(kami.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(kami.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
