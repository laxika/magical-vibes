package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NornsFetchling.class)
class NornsFetchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Without corrupted, entering conjures Plains")
    void withoutCorruptedConjuresPlains() {
        castFetchling();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("With corrupted, declining the seek conjures Plains")
    void decliningSeekConjuresPlains() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("With corrupted, accepting the seek puts a nonland card into hand")
    void acceptingSeekPutsNonlandIntoHand() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        Card creature = new Card();
        creature.setName("Library Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}");
        harness.setLibrary(player1, List.of(creature));

        castFetchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Library Creature");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castFetchling() {
        harness.setHand(player1, List.of(new NornsFetchling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
