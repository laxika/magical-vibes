package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
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

@CardUsed({CollateralDamage.class, GrizzlyBears.class, UginTheSpiritDragon.class})
class CollateralDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and deals 3 damage to a target player")
    void sacrificesCreatureAndDealsDamageToPlayer() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        prepareCard();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareCard();
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        prepareCard();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, player2.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("The sacrifice is paid before damage resolves")
    void sacrificeIsPaidImmediately() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        prepareCard();

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Collateral Damage");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, player2.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Collateral Damage");
    }

    @Test
    @DisplayName("Can target the creature sacrificed to pay the cost, but then has no legal target")
    void canTargetSacrificedCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        prepareCard();

        harness.castInstantWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Collateral Damage");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can deal damage to its controller")
    void dealsDamageToController() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareCard();

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 3 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        target.setCounterCount(CounterType.LOYALTY, 7);
        prepareCard();

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Ugin, the Spirit Dragon");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent")
    void cannotSacrificeNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new UginTheSpiritDragon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        prepareCard();

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, player2.getId(), planeswalker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        harness.assertOnBattlefield(player1, "Ugin, the Spirit Dragon");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Collateral Damage");
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new CollateralDamage()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
