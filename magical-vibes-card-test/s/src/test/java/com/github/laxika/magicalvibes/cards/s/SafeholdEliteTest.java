package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SafeholdElite.class, Eviscerate.class, FaerieMacabre.class})
class SafeholdEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns Safehold Elite with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new SafeholdElite());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Safehold Elite"));
        resolveAllTriggers();

        Permanent elite = findPermanent(player1, "Safehold Elite");
        assertThat(elite).isNotNull();
        assertThat(elite.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elite.getEffectivePower()).isEqualTo(1);
        assertThat(elite.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Safehold Elite when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        elite.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, elite.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertInGraveyard(player1, "Safehold Elite");
    }

    @Test
    @DisplayName("The counter from persist prevents a second return")
    void secondDeathDoesNotReturn() {
        harness.addToBattlefield(player1, new SafeholdElite());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Safehold Elite"));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Safehold Elite");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.castSorcery(player1, 0, 0, returned.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertInGraveyard(player1, "Safehold Elite");
    }

    @Test
    @DisplayName("A stolen Safehold Elite returns under its owner's control")
    void persistReturnsToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, stolen.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        Permanent returned = findPermanent(player2, "Safehold Elite");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Safehold Elite");
        harness.assertNotInGraveyard(player2, "Safehold Elite");
    }

    @Test
    @DisplayName("Lethal -1/-1 counters prevent persist when state-based actions put it in the graveyard")
    void lethalMinusCountersPreventPersist() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        elite.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertInGraveyard(player1, "Safehold Elite");
    }

    @Test
    @DisplayName("Exiling Safehold Elite in response to persist prevents its return")
    void exileInResponsePreventsReturn() {
        SafeholdElite card = new SafeholdElite();
        harness.addToBattlefield(player1, card);
        harness.setHand(player1, List.of(new Eviscerate(), new FaerieMacabre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Safehold Elite"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Safehold Elite");
        assertThat(gd.stack).hasSize(1);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(card.getId()));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertNotInGraveyard(player1, "Safehold Elite");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(card.getId());
    }
}
