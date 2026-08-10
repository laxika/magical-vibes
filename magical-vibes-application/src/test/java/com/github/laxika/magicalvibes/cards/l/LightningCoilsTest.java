package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lightning Coils")
class LightningCoilsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature you control dying adds a charge counter")
    void nontokenCreatureDeathAddsChargeCounter() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new VillageRites()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, bears.getId());
        resolveAllTriggers();

        Permanent coils = findPermanent(player1, "Lightning Coils");
        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A token creature dying does not add a charge counter")
    void tokenCreatureDeathDoesNotAddChargeCounter() {
        harness.addToBattlefield(player1, new LightningCoils());
        harness.setHand(player1, List.of(new ThatcherRevolt(), new VillageRites()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human");
        harness.castSorceryWithSacrifice(player1, 0, token.getId());
        resolveAllTriggers();

        Permanent coils = findPermanent(player1, "Lightning Coils");
        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Five charge counters create that many hasty 3/1 Elementals at upkeep")
    void fiveCountersCreateFiveElementals() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isZero();
        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(5).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getPower()).isEqualTo(3);
            assertThat(elemental.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("Fewer than five charge counters do not create Elementals")
    void fewerThanFiveCountersDoNotCreateElementals() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(coils.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Created Elementals are exiled at the beginning of the next end step")
    void elementalsAreExiledAtNextEndStep() {
        harness.addToBattlefield(player1, new LightningCoils());
        Permanent coils = findPermanent(player1, "Lightning Coils");
        coils.setCounterCount(CounterType.CHARGE, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(5);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }
}
