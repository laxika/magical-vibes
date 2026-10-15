package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkoumBoulderfoot.class, GrizzlyBears.class, CaravanEscort.class, GideonJura.class, Regress.class})
class AkoumBoulderfootTest extends BaseCardTest {

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("ETB deals 1 damage to a target player")
    void etbDealsOneDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AkoumBoulderfoot()));
        addCastingMana();

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Akoum Boulderfoot");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB deals 1 damage to a target creature")
    void etbDealsOneDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new AkoumBoulderfoot()));
        addCastingMana();

        harness.castCreature(player1, 0, 0, bearsId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Akoum Boulderfoot");
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void etbDealsOneDamageToPlaneswalker() {
        harness.addToBattlefieldAndReturn(player2, new GideonJura()).setCounterCount(CounterType.LOYALTY, 6);
        UUID targetId = harness.getPermanentId(player2, "Gideon Jura");
        int loyaltyBefore = findPermanent(player2, "Gideon Jura").getCounterCount(CounterType.LOYALTY);
        harness.setHand(player1, List.of(new AkoumBoulderfoot()));
        addCastingMana();

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Gideon Jura").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(loyaltyBefore - 1);
        harness.assertLife(player2, 20);
    }

    @Test
    void etbKillsCreatureWithOneToughness() {
        harness.addToBattlefield(player2, new CaravanEscort());
        UUID targetId = harness.getPermanentId(player2, "Caravan Escort");
        harness.setHand(player1, List.of(new AkoumBoulderfoot()));
        addCastingMana();

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Caravan Escort");
        harness.assertInGraveyard(player2, "Caravan Escort");
    }

    @Test
    void etbCanTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new AkoumBoulderfoot()));
        addCastingMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Akoum Boulderfoot"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Akoum Boulderfoot").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void etbDoesNothingWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new CaravanEscort());
        UUID targetId = harness.getPermanentId(player2, "Caravan Escort");
        harness.setHand(player1, List.of(new AkoumBoulderfoot(), new Regress()));
        addCastingMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Caravan Escort");
        harness.assertNotInGraveyard(player2, "Caravan Escort");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Akoum Boulderfoot");
    }

    @Test
    void etbStillDealsDamageAfterSourceLeavesBattlefield() {
        harness.setHand(player1, List.of(new AkoumBoulderfoot(), new Regress()));
        addCastingMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Akoum Boulderfoot"));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Akoum Boulderfoot");
        harness.assertNotOnBattlefield(player1, "Akoum Boulderfoot");
        harness.assertLife(player2, 19);
    }
}
