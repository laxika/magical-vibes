package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.k.KalastriaHealer;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuarantineField.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class,
        Naturalize.class, ExpeditionEnvoy.class, KalastriaHealer.class})
class QuarantineFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to one nonland permanent per isolation counter")
    void exilesOnePermanentPerIsolationCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Quarantine Field");
        assertThat(source.getCounterCount(CounterType.ISOLATION)).isEqualTo(2);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(enchantment.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId())
                        || permanent.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Returns all exiled permanents when Quarantine Field leaves")
    void returnsExiledPermanentsWhenSourceLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Quarantine Field");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("Cannot target a land or a permanent you control")
    void rejectsIllegalTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castForX(1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(legalTarget.getId()));
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(legalTarget.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("May choose no targets even with isolation counters")
    void mayChooseNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Expedition Envoy");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
        assertThat(findPermanent(player1, "Quarantine Field").getCounterCount(CounterType.ISOLATION)).isEqualTo(2);
    }

    @Test
    @DisplayName("May exile fewer permanents than the number of isolation counters")
    void mayChooseFewerTargetsThanCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
    }

    @Test
    @DisplayName("Removing isolation counters after choosing targets does not reduce exile")
    void chosenTargetsRemainLockedWhenCountersChange() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        findPermanent(player1, "Quarantine Field").setCounterCount(CounterType.ISOLATION, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();
    }

    @Test
    @DisplayName("X zero leaves opposing permanents untouched")
    void zeroCountersExilesNothing() {
        harness.addToBattlefield(player2, new ExpeditionEnvoy());
        castForX(0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Expedition Envoy");
        assertThat(findPermanent(player1, "Quarantine Field").getCounterCount(CounterType.ISOLATION)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaving before the entry trigger resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        castForX(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, findPermanent(player1, "Quarantine Field").getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quarantine Field");
        harness.assertOnBattlefield(player2, "Expedition Envoy");
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("All exiled Allies return simultaneously and see each other's entry")
    void returningHealerSeesOtherReturningAlly() {
        Permanent envoy = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new KalastriaHealer());
        castForX(2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(envoy.getId(), healer.getId()));
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, findPermanent(player1, "Quarantine Field").getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Expedition Envoy");
        harness.assertOnBattlefield(player2, "Kalastria Healer");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    private void castForX(int xValue) {
        harness.setHand(player1, List.of(new QuarantineField()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        gs.playCard(gd, player1, 0, xValue, null, null, List.of(), List.of());
    }
}
