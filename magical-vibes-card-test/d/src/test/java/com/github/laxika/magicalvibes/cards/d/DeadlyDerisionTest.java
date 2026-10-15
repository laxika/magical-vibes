package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyDerision.class, DarksteelMyr.class, GrizzlyBears.class, JaceBeleren.class, Plains.class})
class DeadlyDerisionTest extends BaseCardTest {

    @Test
    void destroysCreatureAndCreatesTreasure() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castDeadlyDerision(player1, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void destroysPlaneswalkerAndCreatesTreasure() {
        Permanent jace = addReadyJace(player2);

        castDeadlyDerision(player1, jace.getId());

        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.assertNotOnBattlefield(player2, "Jace Beleren");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void cannotTargetNonCreatureNonPlaneswalker() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DeadlyDerision()));
        addDeadlyDerisionMana(player1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void canDestroyOwnCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castDeadlyDerision(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void createsTreasureEvenWhenTargetIsIndestructible() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());

        castDeadlyDerision(player1, myr.getId());

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1)
                .allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Deadly Derision");
    }

    @Test
    void doesNotCreateTreasureWhenTargetLeavesBattlefield() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadlyDerision()));
        addDeadlyDerisionMana(player1);
        harness.castInstant(player1, 0, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerHands.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Deadly Derision");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    private void castDeadlyDerision(Player player, java.util.UUID targetId) {
        harness.setHand(player, List.of(new DeadlyDerision()));
        addDeadlyDerisionMana(player);
        harness.castAndResolveInstant(player, 0, targetId);
    }

    private void addDeadlyDerisionMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        return jace;
    }
}
