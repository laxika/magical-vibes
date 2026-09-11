package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseHarvester.class, GrizzlyBears.class, GravebaneZombie.class, Swamp.class, Forest.class})
class CorpseHarvesterTest extends BaseCardTest {

    @Test
    void searchesForAZombieAndASwamp() {
        addCreatureReady(player1, new CorpseHarvester());
        var sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        setLibrary(new GravebaneZombie(), new Swamp(), new Forest(), new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch zombieSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(zombieSearch).isNotNull();
        assertThat(zombieSearch.params().cards()).allMatch(card -> card instanceof GravebaneZombie);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch swampSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(swampSearch).isNotNull();
        assertThat(swampSearch.params().cards()).allMatch(card -> card instanceof Swamp);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GravebaneZombie)
                .anyMatch(card -> card instanceof Swamp);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card instanceof GravebaneZombie || card instanceof Swamp);
    }

    @Test
    void canSacrificeItself() {
        addCreatureReady(player1, new CorpseHarvester());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Corpse Harvester");
        harness.assertInGraveyard(player1, "Corpse Harvester");
        assertThat(gd.stack).hasSize(1);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
