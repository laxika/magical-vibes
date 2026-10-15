package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PunishingPunch.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Shock.class})
class PunishingPunchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the controlled creature's power to an opposing creature")
    void dealsTwiceControlledCreaturePower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Costs {2} less with two creature cards in your graveyard")
    void costsTwoLessWithTwoCreatureCardsInGraveyard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not reduce its cost with fewer than two creature cards in your graveyard")
    void doesNotReduceCostWithFewerThanTwoCreatureCards() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the second target to be a creature an opponent controls")
    void requiresOpposingCreatureAsSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Requires the first target to be a creature you control")
    void rejectsOpposingCreatureAsFirstTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature cards in an opponent's graveyard do not reduce the cost")
    void opponentGraveyardDoesNotReduceCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals no damage if the source creature is removed in response")
    void removedSourceDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new PunishingPunch()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Punishing Punch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals no damage if the opposing creature is removed in response")
    void removedVictimDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PunishingPunch(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Punishing Punch");
        assertThat(gd.stack).isEmpty();
    }
}
