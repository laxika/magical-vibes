package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PulseOfTheForge.class, ElspethKnightErrant.class, CrazedGoblin.class})
class PulseOfTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage and returns to hand when the opponent still has more life")
    void returnsToHandWhenOpponentStillHasMoreLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(handNames(player1)).containsExactly("Pulse of the Forge");
        assertThat(graveyardNames(player1)).doesNotContain("Pulse of the Forge");
    }

    @Test
    @DisplayName("Goes to the graveyard when the opponent does not have more life after the damage")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreLifeAfterDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 13);
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
        assertThat(handNames(player1)).doesNotContain("Pulse of the Forge");
        assertThat(graveyardNames(player1)).containsExactly("Pulse of the Forge");
    }

    @Test
    @DisplayName("Deals damage to a planeswalker and checks its controller's life")
    void dealsDamageToPlaneswalkerAndChecksControllerLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);

        castAt(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(handNames(player1)).containsExactly("Pulse of the Forge");
    }

    @Test
    @DisplayName("Does not return when targeting your own planeswalker")
    void doesNotReturnWhenTargetingOwnPlaneswalker() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player1, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);

        castAt(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(handNames(player1)).doesNotContain("Pulse of the Forge");
        assertThat(graveyardNames(player1)).containsExactly("Pulse of the Forge");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Crazed Goblin")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnWhenLifeTotalsAreEqualAfterDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 14);

        castAt(player2.getId());

        harness.assertLife(player2, 10);
        harness.assertNotInHand(player1, "Pulse of the Forge");
        harness.assertInGraveyard(player1, "Pulse of the Forge");
    }

    @Test
    void canTargetItsControllerWithoutReturning() {
        harness.setLife(player1, 10);

        castAt(player1.getId());

        harness.assertLife(player1, 6);
        harness.assertNotInHand(player1, "Pulse of the Forge");
        harness.assertInGraveyard(player1, "Pulse of the Forge");
    }

    @Test
    void returnsEvenWhenDamageRemovesAllPlaneswalkerLoyalty() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);

        castAt(elspeth.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Elspeth, Knight-Errant");
        harness.assertInHand(player1, "Pulse of the Forge");
        harness.assertNotInGraveyard(player1, "Pulse of the Forge");
    }

    @Test
    void doesNotReturnWhenOpposingPlaneswalkerControllerHasEqualLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);

        castAt(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 10);
        harness.assertNotInHand(player1, "Pulse of the Forge");
        harness.assertInGraveyard(player1, "Pulse of the Forge");
    }

    @Test
    void checksLifeTotalsAtResolutionRatherThanWhenCast() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();
        harness.castInstant(player1, 0, player2.getId());
        harness.setLife(player1, 17);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertNotInHand(player1, "Pulse of the Forge");
        harness.assertInGraveyard(player1, "Pulse of the Forge");
    }

    @Test
    void doesNotResolveOrReturnWhenPlaneswalkerTargetLeavesBattlefield() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();
        harness.castInstant(player1, 0, elspeth.getId());
        elspeth.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertNotInHand(player1, "Pulse of the Forge");
        harness.assertInGraveyard(player1, "Pulse of the Forge");
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(card -> card.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(card -> card.getName()).toList();
    }
}
