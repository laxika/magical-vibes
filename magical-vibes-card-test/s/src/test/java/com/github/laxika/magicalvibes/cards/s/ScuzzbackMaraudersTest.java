package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuzzbackMarauders.class, Eviscerate.class, FaerieMacabre.class, SilkbindFaerie.class})
class ScuzzbackMaraudersTest extends BaseCardTest {

    @Test
    @DisplayName("Persist returns Scuzzback Marauders with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new ScuzzbackMarauders());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Scuzzback Marauders"));
        resolveAllTriggers();

        Permanent marauders = findPermanent(player1, "Scuzzback Marauders");
        assertThat(marauders).isNotNull();
        assertThat(marauders.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(marauders.getEffectivePower()).isEqualTo(4);
        assertThat(marauders.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Persist does not return Scuzzback Marauders when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent marauders = harness.addToBattlefieldAndReturn(player1, new ScuzzbackMarauders());
        marauders.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, marauders.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Scuzzback Marauders");
        harness.assertInGraveyard(player1, "Scuzzback Marauders");
    }

    @Test
    void persistedCreatureDoesNotReturnAfterDyingAgain() {
        harness.addToBattlefield(player1, new ScuzzbackMarauders());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Scuzzback Marauders"));
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Scuzzback Marauders");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castSorcery(player1, 0, 0, returned.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Scuzzback Marauders");
        harness.assertInGraveyard(player1, "Scuzzback Marauders");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void persistCannotReturnCardExiledInResponse() {
        ScuzzbackMarauders card = new ScuzzbackMarauders();
        harness.addToBattlefield(player1, card);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.setHand(player2, List.of(new FaerieMacabre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Scuzzback Marauders"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Scuzzback Marauders");
        assertThat(gd.stack).hasSize(1);

        harness.activateHandAbilityWithGraveyardTargets(player2, 0, List.of(card.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Scuzzback Marauders");
        harness.assertNotInGraveyard(player1, "Scuzzback Marauders");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(card.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ScuzzbackMarauders.class, SilkbindFaerie.class})
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ScuzzbackMarauders());
        Permanent blocker = addCreatureReady(player2, new SilkbindFaerie());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Silkbind Faerie");
        harness.assertOnBattlefield(player1, "Scuzzback Marauders");
    }

    @Test
    @CardUsed({ScuzzbackMarauders.class})
    void persistReturnsToOwnerRatherThanController() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new ScuzzbackMarauders());
        gd.stolenCreatures.put(stolen.getId(), player1.getId());
        stolen.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Scuzzback Marauders");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Scuzzback Marauders");
        Permanent returned = findPermanent(player1, "Scuzzback Marauders");
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Scuzzback Marauders");
    }
}
