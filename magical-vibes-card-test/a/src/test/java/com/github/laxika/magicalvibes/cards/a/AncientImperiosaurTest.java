package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FinalFlourish;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientImperiosaur.class, PortentTracker.class, FinalFlourish.class})
class AncientImperiosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters for each creature that convoked it")
    void entersWithCountersForConvokeCreatures() {
        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        Permanent thirdConvokeCreature = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        harness.setHand(player1, List.of(new AncientImperiosaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId(), thirdConvokeCreature.getId()));
        harness.passBothPriorities();

        Permanent imperiosaur = findPermanent(player1, "Ancient Imperiosaur");
        assertThat(imperiosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(firstConvokeCreature.isTapped()).isTrue();
        assertThat(secondConvokeCreature.isTapped()).isTrue();
        assertThat(thirdConvokeCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters without counters when no creature convoked it")
    void entersWithoutCountersWhenNotConvoked() {
        harness.castFromHand(player1, new AncientImperiosaur(), "{5}{G}{G}");
        harness.passBothPriorities();

        Permanent imperiosaur = findPermanent(player1, "Ancient Imperiosaur");
        assertThat(imperiosaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canConvokeEntireCostWithSummoningSickCreatures() {
        List<Permanent> creatures = IntStream.range(0, 7)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new PortentTracker()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new AncientImperiosaur()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ancient Imperiosaur")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(14);
        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    void countsConvokeCreatureThatDiesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        harness.setHand(player1, List.of(new AncientImperiosaur()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId()));

        harness.setHand(player2, List.of(new FinalFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Portent Tracker");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ancient Imperiosaur")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void wardCountersOpponentSpellWhenTheyCannotPay() {
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player1, new AncientImperiosaur());
        harness.setHand(player2, List.of(new FinalFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, imperiosaur.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Final Flourish");
        assertThat(gqs.getEffectiveToughness(gd, imperiosaur)).isEqualTo(6);
    }

    @Test
    void cannotConvokeSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        harness.setHand(player1, List.of(new AncientImperiosaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotConvokeMoreCreaturesThanTotalManaCost() {
        List<UUID> creatures = IntStream.range(0, 8)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new PortentTracker()).getId())
                .toList();
        harness.setHand(player1, List.of(new AncientImperiosaur()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(), creatures))
                .isInstanceOf(IllegalStateException.class);
    }
}
