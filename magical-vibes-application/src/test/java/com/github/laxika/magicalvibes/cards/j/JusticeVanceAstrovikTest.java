package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JusticeVanceAstrovik.class, Boomerang.class, GrizzlyBears.class, Island.class})
class JusticeVanceAstrovikTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target nonland, nontoken permanent")
    void etbReturnsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JusticeVanceAstrovik()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanChooseNoTarget() {
        harness.setHand(player1, List.of(new JusticeVanceAstrovik()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Justice, Vance Astrovik");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning another nonland permanent you control puts a +1/+1 counter on Justice")
    void returningAnotherControlledPermanentPutsCounterOnJustice() {
        Permanent justice = harness.addToBattlefieldAndReturn(player1, new JusticeVanceAstrovik());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveBoomerang(target.getId());

        assertThat(justice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The return trigger ignores lands, opponent permanents, and Justice itself")
    void returnTriggerIgnoresNonMatchingPermanents() {
        Permanent justice = harness.addToBattlefieldAndReturn(player1, new JusticeVanceAstrovik());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        castAndResolveBoomerang(island.getId());
        assertThat(justice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        castAndResolveBoomerang(token.getId());
        assertThat(justice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolveBoomerang(opponentPermanent.getId());
        assertThat(justice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        castBoomerangAndResolveSpell(justice.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target a token")
    void etbRejectsTokenTarget() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        harness.setHand(player1, List.of(new JusticeVanceAstrovik()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken");
    }

    private void castAndResolveBoomerang(UUID targetId) {
        castBoomerangAndResolveSpell(targetId);
        harness.passBothPriorities();
    }

    private void castBoomerangAndResolveSpell(UUID targetId) {
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
