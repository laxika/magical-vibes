package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolphimMayhemDominus.class, Blaze.class, GrizzlyBears.class})
class SolphimMayhemDominusTest extends BaseCardTest {

    @Test
    void doublesNoncombatDamageToOpponent() {
        harness.addToBattlefield(player1, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    void doesNotDoubleCombatDamage() {
        harness.addToBattlefield(player1, new SolphimMayhemDominus());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void discardsTwoCardsAndPutsAnIndestructibleCounterOnIt() {
        Permanent solphim = harness.addToBattlefieldAndReturn(player1, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(solphim.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void doublesNoncombatDamageToOpponentsCreature() {
        harness.addToBattlefield(player1, new SolphimMayhemDominus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1,
                harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNotDoubleDamageToOwnCreature() {
        harness.addToBattlefield(player1, new SolphimMayhemDominus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotDoubleDamageToItsController() {
        harness.addToBattlefield(player1, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    void doesNotDoubleDamageFromAnOpponentsSource() {
        harness.addToBattlefield(player2, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void canPayBothPhyrexianSymbolsWithLife() {
        Permanent solphim = harness.addToBattlefieldAndReturn(player1, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Blaze()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(solphim.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    void indestructibleCounterProtectsAgainstLethalDamage() {
        Permanent solphim = harness.addToBattlefieldAndReturn(player1, new SolphimMayhemDominus());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Blaze()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 4, solphim.getId());

        harness.assertOnBattlefield(player1, "Solphim, Mayhem Dominus");
        assertThat(solphim.getMarkedDamage()).isEqualTo(4);
    }
}
