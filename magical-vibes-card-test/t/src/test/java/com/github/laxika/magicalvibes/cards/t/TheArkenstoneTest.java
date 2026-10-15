package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BomburGentleDreamer;
import com.github.laxika.magicalvibes.cards.b.BejeweledWarg;
import com.github.laxika.magicalvibes.cards.k.KarnsTouch;
import com.github.laxika.magicalvibes.cards.s.SeekTheHeart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheArkenstone.class, SeekTheHeart.class, BomburGentleDreamer.class, BejeweledWarg.class, KarnsTouch.class})
class TheArkenstoneTest extends BaseCardTest {

    @Test
    void boostsOnlyCreaturesYouControl() {
        harness.addToBattlefield(player1, new TheArkenstone());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BejeweledWarg());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BejeweledWarg());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void drawsAtBeginningOfControllersEndStep() {
        harness.addToBattlefield(player1, new TheArkenstone());
        Card drawn = new BejeweledWarg();
        harness.setLibrary(player1, List.of(drawn));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void seekTheHeartSearchesForALegendaryCreature() {
        Card nonLegendaryCreature = new BejeweledWarg();
        Card legendaryCreature = new BomburGentleDreamer();
        TheArkenstone card = new TheArkenstone();
        harness.setLibrary(player1, List.of(nonLegendaryCreature, legendaryCreature));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(legendaryCreature);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLegendaryCreature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void animatedArkenstoneReceivesItsOwnBoost() {
        Permanent arkenstone = harness.addToBattlefieldAndReturn(player1, new TheArkenstone());
        harness.setHand(player1, List.of(new KarnsTouch()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, arkenstone.getId());

        assertThat(gqs.getEffectivePower(gd, arkenstone)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, arkenstone)).isEqualTo(6);
    }

    @Test
    void doesNotDrawAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new TheArkenstone());
        Card drawn = new BejeweledWarg();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void canFailToFindAndCastArtifactFromAdventureExile() {
        TheArkenstone card = new TheArkenstone();
        Card legendaryNoncreature = new TheArkenstone();
        Card nonlegendaryCreature = new BejeweledWarg();
        harness.setLibrary(player1, List.of(legendaryNoncreature, nonlegendaryCreature));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(legendaryNoncreature, nonlegendaryCreature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Arkenstone");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void mayFailToFindEvenWhenLegendaryCreatureExists() {
        TheArkenstone card = new TheArkenstone();
        Card legendaryCreature = new BomburGentleDreamer();
        harness.setLibrary(player1, List.of(legendaryCreature));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(legendaryCreature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
