package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BedeckBedazzle.class, SenateCourier.class, RakdosGuildgate.class, Plains.class, DovinGrandArbiter.class})
class BedeckBedazzleTest extends BaseCardTest {

    private static final int BEDECK = 0;
    private static final int BEDAZZLE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Bedeck gives the targeted creature +3/-3 until end of turn")
    void bedeckBoostsAndWeakensCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, BEDECK, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bedeck wears off at end of turn")
    void bedeckWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, BEDECK, creature.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Bedazzle destroys a nonbasic land and deals 2 damage to an opponent")
    void bedazzleDestroysLandAndDamagesOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, BEDAZZLE, List.of(land.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rakdos Guildgate");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Both halves cannot be cast together because this card has no fuse")
    void cannotCastBothHalvesTogether() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FUSE,
                List.of(creature.getId(), land.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedazzle cannot target a basic land")
    void bedazzleCannotTargetBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, BEDAZZLE, List.of(land.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedeck cannot target a player")
    void bedeckCannotTargetPlayer() {
        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, BEDECK, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedazzle can damage its controller's planeswalker")
    void bedazzleDamagesOwnPlaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        Permanent dovin = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());
        dovin.setCounterCount(CounterType.LOYALTY, 3);
        prepareBedazzle();

        harness.castModalInstant(player1, 0, BEDAZZLE, List.of(land.getId(), dovin.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rakdos Guildgate");
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Bedazzle still damages the opponent when the land target disappears")
    void bedazzleResolvesWithOnlyOpponentStillLegal() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        prepareBedazzle();
        harness.castModalInstant(player1, 0, BEDAZZLE, List.of(land.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Bedazzle still destroys the land when the planeswalker target disappears")
    void bedazzleResolvesWithOnlyLandStillLegal() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        Permanent dovin = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        prepareBedazzle();
        harness.castModalInstant(player1, 0, BEDAZZLE, List.of(land.getId(), dovin.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(dovin);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rakdos Guildgate");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Bedazzle cannot damage its controller")
    void bedazzleCannotTargetController() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        prepareBedazzle();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, BEDAZZLE,
                List.of(land.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedazzle requires both targets when cast")
    void bedazzleCannotBeCastWithoutLandTarget() {
        prepareBedazzle();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, BEDAZZLE,
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedazzle cannot damage a creature")
    void bedazzleCannotTargetCreatureForDamage() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SenateCourier());
        prepareBedazzle();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, BEDAZZLE,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedeck can be paid for entirely with red mana")
    void bedeckAcceptsRedMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SenateCourier());
        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, BEDECK, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    private void prepareBedazzle() {
        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
