package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuandrixCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and puts counters on another creature")
    void returnsCreatureAndPutsCountersOnAnotherCreature() {
        Permanent toReturn = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent toBoost = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(toReturn.getId(), toBoost.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(toBoost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters an artifact spell and shuffles up to three cards from a target player's graveyard")
    void countersArtifactSpellAndShufflesGraveyardCards() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Card graveyardFirst = new GrizzlyBears();
        Card graveyardSecond = new Shock();
        Card graveyardThird = new Forest();
        harness.setGraveyard(player1, List.of(graveyardFirst, graveyardSecond, graveyardThird));
        harness.setLibrary(player1, List.of(new Forest()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, spellbook.getId(),
                List.of(player1.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardFirst.getId(), graveyardSecond.getId(),
                graveyardThird.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 3);
    }

    @Test
    @DisplayName("The counter mode rejects a creature spell")
    void counterModeRejectsCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new QuandrixCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() ->
                harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, bears.getId(),
                        List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
