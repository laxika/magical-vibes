package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.k.KaitoDancingShadow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SheoldredsEdict.class, GrizzlyBears.class, KaitoDancingShadow.class})
class SheoldredsEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creature mode sacrifices one nontoken creature per opponent")
    void sacrificesNontokenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, creatureToken("Bear Token"));

        cast(0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Bear Token")).hasSize(1);
    }

    @Test
    @DisplayName("Creature token mode sacrifices one creature token per opponent")
    void sacrificesCreatureToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, creatureToken("Bear Token"));

        cast(1);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Bear Token")).isEmpty();
    }

    @Test
    @DisplayName("Planeswalker mode sacrifices one planeswalker per opponent")
    void sacrificesPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addPlaneswalker();

        cast(2);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Test Planeswalker")).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which nontoken creature to sacrifice")
    void opponentChoosesNontokenCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent token = harness.addToBattlefieldAndReturn(player2, creatureToken("Bear Token"));

        cast(0);
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, token).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(chosen.getCard());
    }

    @Test
    @DisplayName("Opponent chooses exactly one creature token and controller keeps their token")
    void opponentChoosesCreatureToken() {
        Permanent ownToken = harness.addToBattlefieldAndReturn(player1, creatureToken("Own Bear"));
        Permanent first = harness.addToBattlefieldAndReturn(player2, creatureToken("First Bear"));
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, creatureToken("Chosen Bear"));
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(1);
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownToken);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(chosen);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Planeswalker mode sacrifices a real planeswalker and leaves the controller's intact")
    void sacrificesOnlyOpponentsRealPlaneswalker() {
        Permanent ownWalker = harness.addToBattlefieldAndReturn(player1, new KaitoDancingShadow());
        ownWalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent opposingWalker = harness.addToBattlefieldAndReturn(player2, new KaitoDancingShadow());
        opposingWalker.setCounterCount(CounterType.LOYALTY, 3);

        cast(2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownWalker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingWalker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingWalker.getCard());
    }

    @Test
    @DisplayName("Nontoken mode resolves without sacrificing when only creature tokens are present")
    void nontokenModeWithNoEligibleCreature() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, creatureToken("Bear Token"));

        cast(0);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(token);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature token mode leaves nontoken creatures and noncreature tokens intact")
    void tokenModeWithNoEligibleCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card treasure = new Card();
        treasure.setName("Treasure");
        treasure.setType(CardType.ARTIFACT);
        treasure.setToken(true);
        Permanent treasurePermanent = harness.addToBattlefieldAndReturn(player2, treasure);

        cast(1);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear, treasurePermanent);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Planeswalker mode resolves when the opponent controls only creatures")
    void planeswalkerModeWithNoEligiblePermanent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(2);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bear);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new SheoldredsEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}{B}");
        card.setLoyalty(4);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        return permanent;
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
