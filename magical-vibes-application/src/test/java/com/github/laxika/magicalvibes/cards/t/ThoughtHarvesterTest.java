package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaintersServant;
import com.github.laxika.magicalvibes.cards.p.ProphetOfDistortion;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtHarvester.class, Forest.class, GrizzlyBears.class, Spellbook.class,
        ProphetOfDistortion.class, PaintersServant.class})
class ThoughtHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a colorless spell exiles the targeted opponent's top card")
    void colorlessSpellExilesOpponentsTopCard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new ThoughtHarvester());
        Card topCard = new Forest();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.castFromHand(player1, new Spellbook(), "{0}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(harvester);
    }

    @Test
    @DisplayName("Casting a colored spell does not trigger Thought Harvester")
    void coloredSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Thought Harvester's trigger can target only an opponent")
    void triggerCannotTargetController() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        harness.castFromHand(player1, new Spellbook(), "{0}");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("A devoid spell triggers despite requiring colored mana")
    void devoidSpellTriggers() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        Card topCard = new ThoughtHarvester();
        harness.setLibrary(player2, List.of(topCard));

        harness.castFromHand(player1, new ProphetOfDistortion(), "{U}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Prophet of Distortion");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prophet of Distortion");
    }

    @Test
    @DisplayName("An opponent's colorless spell does not trigger")
    void opponentsColorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new ThoughtHarvester());
        Card topCard = new ThoughtHarvester();
        harness.setLibrary(player1, List.of(topCard));

        harness.castFromHand(player1, new ProphetOfDistortion(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertOnBattlefield(player1, "Prophet of Distortion");
    }

    @Test
    @DisplayName("An empty library remains a legal target and causes no life change")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        harness.setLibrary(player2, List.of());
        int life = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new ProphetOfDistortion(), "{U}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(life);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prophet of Distortion");
    }

    @Test
    @DisplayName("Thought Harvester does not trigger from its own cast")
    void doesNotTriggerFromItsOwnCast() {
        Card topCard = new ThoughtHarvester();
        harness.setLibrary(player2, List.of(topCard));

        harness.castFromHand(player1, new ThoughtHarvester(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thought Harvester");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Painter's Servant makes a devoid spell colored so it does not trigger")
    void paintersServantPreventsColorlessTrigger() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        Permanent servant = harness.addToBattlefieldAndReturn(player1, new PaintersServant());
        servant.setChosenColor(CardColor.BLUE);
        Card topCard = new ThoughtHarvester();
        harness.setLibrary(player2, List.of(topCard));

        harness.castFromHand(player1, new ProphetOfDistortion(), "{U}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Prophet of Distortion");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }
}
