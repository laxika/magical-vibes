package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormFleetNegotiator.class, GrizzlyBears.class, Forest.class})
class StormFleetNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Map for each revealed nonland and each player draws")
    void createsMapsForNonlandsAndDraws() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking creates no Map for revealed lands but each player still draws")
    void skipsMapsForLandsAndDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mixed reveals create one Map for the attacker and draw the revealed cards")
    void mixedRevealsCreateOneMap() {
        var nonland = new StormFleetNegotiator();
        var land = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(nonland, new Forest()));
        harness.setLibrary(player2, List.of(land, new StormFleetNegotiator()));
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
        assertThat(findPermanents(player2, "Map")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Maps belong to the trigger controller when the second player attacks")
    void secondPlayerCreatesMaps() {
        harness.setLibrary(player1, List.of(new StormFleetNegotiator(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        addCreatureReady(player2, new StormFleetNegotiator());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(findPermanents(player2, "Map")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Parley reads the top cards at resolution rather than when the attack is declared")
    void revealsCurrentTopCardsAtResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addCreatureReady(player1, new StormFleetNegotiator());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Map")).isEmpty();
        harness.setLibrary(player2, List.of(new StormFleetNegotiator()));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Each attacking Negotiator resolves its own parley using successive top cards")
    void multipleAttackersParleySeparately() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new StormFleetNegotiator(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new StormFleetNegotiator(), new Forest(), new Forest()));
        addCreatureReady(player1, new StormFleetNegotiator());
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A created Map can be sacrificed for one mana to make a controlled creature explore")
    void createdMapExploresLand() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StormFleetNegotiator(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        var negotiator = addCreatureReady(player1, new StormFleetNegotiator());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Map")).hasSize(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int mapIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Map"));
        harness.activateAbility(player1, mapIndex, null, negotiator.getId());
        assertThat(findPermanents(player1, "Map")).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
