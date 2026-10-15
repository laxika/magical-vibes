package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalIzzetViceroy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InescapableBlaze.class, Cancel.class, GrizzlyBears.class, RalIzzetViceroy.class})
class InescapableBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to target player")
    void dealsSixDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InescapableBlaze()));
        addBlazeMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Deals 6 damage to target creature")
    void dealsSixDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InescapableBlaze()));
        addBlazeMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        InescapableBlaze blaze = new InescapableBlaze();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(blaze));
        addBlazeMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, blaze.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Deals 6 damage directly to a planeswalker")
    void dealsSixDamageToPlaneswalker() {
        var ral = harness.addToBattlefieldAndReturn(player2, new RalIzzetViceroy());
        ral.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new InescapableBlaze()));
        addBlazeMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Ral, Izzet Viceroy"));

        harness.assertNotOnBattlefield(player2, "Ral, Izzet Viceroy");
        harness.assertInGraveyard(player2, "Ral, Izzet Viceroy");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target its controller")
    void canDamageItsController() {
        harness.setHand(player1, List.of(new InescapableBlaze()));
        addBlazeMana();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not resolve when its only target has left the battlefield")
    void doesNotResolveWithMissingTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        InescapableBlaze first = new InescapableBlaze();
        InescapableBlaze second = new InescapableBlaze();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void addBlazeMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
