package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravelgillAxeshark.class, Eviscerate.class})
class GravelgillAxesharkTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns Gravelgill Axeshark with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new GravelgillAxeshark());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Gravelgill Axeshark"));
        resolveAllTriggers();

        Permanent axeshark = findPermanent(player1, "Gravelgill Axeshark");
        assertThat(axeshark).isNotNull();
        assertThat(axeshark.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(axeshark.getEffectivePower()).isEqualTo(2);
        assertThat(axeshark.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Persist does not return Gravelgill Axeshark when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent axeshark = harness.addToBattlefieldAndReturn(player1, new GravelgillAxeshark());
        axeshark.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, axeshark.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Gravelgill Axeshark");
        harness.assertInGraveyard(player1, "Gravelgill Axeshark");
    }

    @Test
    @DisplayName("A second death after persisting does not return the creature again")
    void secondDeathDoesNotReturnAgain() {
        harness.addToBattlefield(player1, new GravelgillAxeshark());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Gravelgill Axeshark"));
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Gravelgill Axeshark");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castSorcery(player1, 0, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gravelgill Axeshark");
        harness.assertInGraveyard(player1, "Gravelgill Axeshark");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Persist returns a stolen creature under its owner's control")
    void persistReturnsToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new GravelgillAxeshark());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, stolen.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gravelgill Axeshark");
        Permanent returned = findPermanent(player1, "Gravelgill Axeshark");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Gravelgill Axeshark");
    }
}
