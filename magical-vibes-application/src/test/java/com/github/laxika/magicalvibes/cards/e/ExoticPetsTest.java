package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RhoxPummeler;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExoticPets.class, RhoxPummeler.class, Island.class})
class ExoticPetsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two unblockable Fish and puts each controlled counter kind on either one")
    void createsFishAndCopiesControlledCounterKinds() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        firstCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondCreature.setCounterCount(CounterType.CHARGE, 1);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(fish).hasSize(2);
        assertThat(fish).allSatisfy(token -> assertThat(gqs.hasCantBeBlocked(gd, token)).isTrue());

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrderElementsOf(
                fish.stream().map(Permanent::getId).toList());

        harness.handlePermanentChosen(player1, fish.getFirst().getId());
        harness.handlePermanentChosen(player1, fish.getFirst().getId());

        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)
                + fish.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.CHARGE)
                + fish.get(1).getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(fish.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(fish.get(1).getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Copies no counter kinds from creatures controlled by an opponent")
    void ignoresOpponentsCounters() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RhoxPummeler());
        opponentCreature.setCounterCount(CounterType.CHARGE, 1);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(fish).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(fish).allSatisfy(token -> {
            assertThat(token.getCounterCount(CounterType.CHARGE)).isZero();
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
    }

    @Test
    @DisplayName("Each kind is copied once even when several creatures have multiple counters")
    void copiesEachKindOnceAndCanSplitThemBetweenFish() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.SHIELD, 2);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(fish).hasSize(2);
        harness.handlePermanentChosen(player1, fish.getFirst().getId());
        harness.handlePermanentChosen(player1, fish.get(1).getId());

        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(fish.getFirst().getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(fish.get(1).getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(fish.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters on controlled noncreatures are ignored")
    void ignoresNoncreatureCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.setCounterCount(CounterType.CHARGE, 2);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(fish).hasSize(2);
        assertThat(fish).allSatisfy(token -> assertThat(token.getCounterCount(CounterType.CHARGE)).isZero());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Fish survives receiving both a -1/-1 and a +1/+1 counter during resolution")
    void defersStateBasedActionsUntilAllCounterKindsArePlaced() {
        Permanent negative = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        Permanent positive = harness.addToBattlefieldAndReturn(player1, new RhoxPummeler());
        negative.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        positive.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(fish).hasSize(2);
        harness.handlePermanentChosen(player1, fish.getFirst().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(fish);
        harness.handlePermanentChosen(player1, fish.getFirst().getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(fish);
        assertThat(fish.getFirst().getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(fish.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.castFromHand(player1, new ExoticPets(), "{1}{W}{U}");
        harness.passBothPriorities();
    }
}
