package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasrisAcolyte.class, GrizzlyBears.class, LlanowarElves.class})
class BasrisAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of up to two other creatures you control")
    void putsCountersOnTwoOtherControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");
        harness.castCreature(player1, 0, List.of(bearsId, elvesId));

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Llanowar Elves")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can choose only one other creature")
    void canChooseOneOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castCreature(player1, 0, List.of(bearsId));

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast without targets")
    void canBeCastWithoutTargets() {
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Basri's Acolyte");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Basri's Acolyte")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target an opposing creature")
    void cannotTargetOpposingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID opposingCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opposingCreatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    void canChooseZeroTargetsEvenWhenOtherCreaturesAreAvailable() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Basri's Acolyte");
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnotherAcolyteButDoesNotPutACounterOnItself() {
        harness.addToBattlefield(player1, new BasrisAcolyte());
        Permanent other = findPermanent(player1, "Basri's Acolyte");
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of(other.getId()));
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Basri's Acolyte")).hasSize(2);
        assertThat(findPermanents(player1, "Basri's Acolyte").stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .mapToInt(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)))
                .containsExactly(0);
    }

    @Test
    void stillCountersRemainingTargetWhenOneTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent elves = findPermanent(player1, "Llanowar Elves");
        harness.setHand(player1, List.of(new BasrisAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        resolveAllTriggers();

        assertThat(elves.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void combatDamageGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BasrisAcolyte());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
