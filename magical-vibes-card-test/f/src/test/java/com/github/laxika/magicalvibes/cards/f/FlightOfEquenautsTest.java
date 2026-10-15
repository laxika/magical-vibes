package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlightOfEquenauts.class, GrizzlyBears.class, VernadiShieldmate.class})
class FlightOfEquenautsTest extends BaseCardTest {

    @Test
    @DisplayName("Convoke taps creatures to help pay the cost")
    void castsWithConvoke() {
        List<Permanent> convokeCreatures = List.of(
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        harness.setHand(player1, List.of(new FlightOfEquenauts()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                convokeCreatures.stream().map(Permanent::getId).toList());

        assertThat(convokeCreatures).allMatch(Permanent::isTapped);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flight of Equenauts");
    }

    @Test
    void castsWithoutConvoke() {
        harness.castFromHand(player1, new FlightOfEquenauts(), "{7}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flight of Equenauts");
    }

    @Test
    void whiteCreaturesCanPayTheEntireCostDespiteSummoningSickness() {
        List<Permanent> creatures = IntStream.range(0, 8)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate()))
                .toList();
        creatures.forEach(creature -> creature.setSummoningSick(true));
        harness.setHand(player1, List.of(new FlightOfEquenauts()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList());

        assertThat(creatures).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Flight of Equenauts");
    }

    @Test
    void cannotConvokeWithAnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        creature.tap();
        harness.setHand(player1, List.of(new FlightOfEquenauts()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Flight of Equenauts");
    }

    @Test
    void cannotTapMoreCreaturesThanTheCostCanPayFor() {
        List<Permanent> creatures = IntStream.range(0, 9)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate()))
                .toList();
        harness.setHand(player1, List.of(new FlightOfEquenauts()));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creatures).noneMatch(Permanent::isTapped);
        harness.assertInHand(player1, "Flight of Equenauts");
    }

    @Test
    void groundCreatureCannotBlockIt() {
        addCreatureReady(player1, new FlightOfEquenauts());
        addCreatureReady(player2, new VernadiShieldmate());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void flyingCreatureCanBlockIt() {
        addCreatureReady(player1, new FlightOfEquenauts());
        Permanent blocker = addCreatureReady(player2, new FlightOfEquenauts());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
