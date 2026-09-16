package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MammothSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImotiCelebrantOfBounty.class, CrawWurm.class, HillGiant.class, LlanowarElves.class,
        MammothSpider.class})
class ImotiCelebrantOfBountyTest extends BaseCardTest {

    @Test
    @DisplayName("Imoti has cascade when cast")
    void ownCascadeTriggersWhenCast() {
        prepareTurn(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new HillGiant());

        harness.setHand(player1, List.of(new ImotiCelebrantOfBounty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("Spells with mana value 6 or greater get cascade")
    void highManaValueSpellGetsCascade() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());

        // Craw Wurm has mana value 6 and Imoti has mana value 5. The MV-5 Mammoth Spider must be
        // offered, proving the cascade threshold is taken from the triggering spell.
        MammothSpider hit = new MammothSpider();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(hit, new LlanowarElves()));

        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Mammoth Spider");
    }

    @Test
    @DisplayName("Spells with mana value less than 6 do not get cascade")
    void lowerManaValueSpellDoesNotGetCascade() {
        prepareTurn(player1);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new LlanowarElves());

        harness.setHand(player1, List.of(new MammothSpider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Imoti does not grant cascade to an opponent's spell")
    void opponentSpellDoesNotGetCascade() {
        prepareTurn(player2);
        harness.addToBattlefield(player1, new ImotiCelebrantOfBounty());
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new LlanowarElves());

        harness.setHand(player2, List.of(new CrawWurm()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void prepareTurn(Player activePlayer) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(activePlayer);
    }
}
