package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtHarvester.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class ThoughtHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a colorless spell exiles the targeted opponent's top card")
    void colorlessSpellExilesOpponentsTopCard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new ThoughtHarvester());
        Card topCard = new Forest();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Thought Harvester's trigger can target only an opponent")
    void triggerCannotTargetController() {
        harness.addToBattlefield(player1, new ThoughtHarvester());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
