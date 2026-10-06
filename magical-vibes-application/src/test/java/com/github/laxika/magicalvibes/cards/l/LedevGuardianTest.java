package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealersHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LedevGuardian.class, GrizzlyBears.class, HealersHawk.class})
class LedevGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the generic cost")
    void castsWithConvoke() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ledev Guardian")).isEqualTo(1);
    }

    @Test
    void whiteCreaturePaysTheWhiteCost() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(hawk.getId()));

        assertThat(hawk.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Ledev Guardian")).isEqualTo(1);
    }

    @Test
    void summoningSickCreaturesCanPayTheEntireCost() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new HealersHawk()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new LedevGuardian()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Ledev Guardian")).isEqualTo(1);
    }

    @Test
    void castsWithoutConvoking() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hawk.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Ledev Guardian")).isEqualTo(1);
    }

    @Test
    void greenCreatureCannotPayTheWhiteCost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void tappedCreatureCannotConvoke() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        hawk.tap();
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(hawk.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsCreatureCannotConvoke() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new HealersHawk());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(hawk.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hawk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void sameCreatureCannotConvokeTwice() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new HealersHawk());
        harness.setHand(player1, List.of(new LedevGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(hawk.getId(), hawk.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hawk.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
