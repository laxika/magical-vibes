package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlowingOne.class, Millstone.class, GrizzlyBears.class, Forest.class, RestInPeace.class})
class GlowingOneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives the damaged player four rad counters")
    void combatDamageGivesFourRadCounters() {
        addCreatureReady(player1, new GlowingOne());

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Gains one life for each nonland card milled")
    void gainsOneLifeForEachNonlandCardMilled() {
        harness.addToBattlefield(player1, new GlowingOne());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Milling only lands does not gain life")
    void millingOnlyLandsDoesNotGainLife() {
        harness.addToBattlefield(player1, new GlowingOne());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent milling nonlands gains life only for Glowing One's controller")
    void opponentMillingGainsLifeForController() {
        harness.addToBattlefield(player1, new GlowingOne());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GlowingOne(), new GlowingOne()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A mixed mill gains life only for its nonland card")
    void mixedMillGainsLifeOnlyForNonland() {
        harness.addToBattlefield(player1, new GlowingOne());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new GlowingOne()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Nonland cards milled into exile still trigger life gain")
    void nonlandsMilledIntoExileStillGainLife() {
        harness.addToBattlefield(player1, new GlowingOne());
        harness.addToBattlefield(player2, new RestInPeace());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new GlowingOne(), new GlowingOne()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.assertLife(player1, 22);
    }
}
