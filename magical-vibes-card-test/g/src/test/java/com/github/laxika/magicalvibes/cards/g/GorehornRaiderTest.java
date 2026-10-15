package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GorehornRaider.class, GrizzlyBears.class})
class GorehornRaiderTest extends BaseCardTest {

    @Test
    void etbDeals2DamageToCreatureWithRaid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        markAttackedThisTurn();
        castGorehornRaider();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void etbDeals2DamageToPlayerWithRaid() {
        harness.setLife(player2, 20);
        markAttackedThisTurn();
        castGorehornRaider();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void etbDoesNotTriggerWithoutRaid() {
        harness.setLife(player2, 20);
        castGorehornRaider();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Gorehorn Raider");
    }

    @Test
    void etbDoesNotTriggerWhenOnlyOpponentAttacked() {
        harness.setLife(player2, 20);
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castGorehornRaider();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Gorehorn Raider");
    }

    @Test
    void etbCanTargetItsControllerWithRaid() {
        harness.setLife(player1, 20);
        markAttackedThisTurn();
        castGorehornRaider();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void etbCanTargetItselfWithRaid() {
        markAttackedThisTurn();
        castGorehornRaider();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gorehorn Raider"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gorehorn Raider");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(2);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void castGorehornRaider() {
        harness.setHand(player1, List.of(new GorehornRaider()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
    }
}
