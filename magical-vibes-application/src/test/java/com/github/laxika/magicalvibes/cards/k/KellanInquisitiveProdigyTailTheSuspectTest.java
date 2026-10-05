package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GildedLotus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IzzetCluestone;
import com.github.laxika.magicalvibes.cards.t.TailTheSuspect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KellanInquisitiveProdigyTailTheSuspect.class, TailTheSuspect.class,
        IzzetCluestone.class, GildedLotus.class, GrizzlyBears.class, Forest.class})
class KellanInquisitiveProdigyTailTheSuspectTest extends BaseCardTest {

    @Test
    @DisplayName("Kellan destroys an artifact I control and draws a card")
    void destroysOwnArtifactAndDraws() {
        addCreatureReady(player1, new KellanInquisitiveProdigyTailTheSuspect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Izzet Cluestone");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Kellan destroys an opponent's artifact without drawing")
    void destroysOpponentsArtifactWithoutDrawing() {
        addCreatureReady(player1, new KellanInquisitiveProdigyTailTheSuspect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedLotus());
        Card notDrawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(notDrawn));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gilded Lotus");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notDrawn);
    }

    @Test
    @DisplayName("Adventure investigates, grants an additional land play, then exiles for the creature cast")
    void adventureResolvesAndCreatureCanBeCastFromExile() {
        KellanInquisitiveProdigyTailTheSuspect card = new KellanInquisitiveProdigyTailTheSuspect();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.additionalLandsThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
        harness.assertNotInGraveyard(player1, "Kellan, Inquisitive Prodigy");

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kellan, Inquisitive Prodigy");
    }

    @Test
    void mayChooseNoArtifactWithoutDrawing() {
        addCreatureReady(player1, new KellanInquisitiveProdigyTailTheSuspect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Card undrawn = new KellanInquisitiveProdigyTailTheSuspect();
        harness.setLibrary(player1, List.of(undrawn));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void drawsEvenWhenOwnArtifactIsIndestructible() {
        addCreatureReady(player1, new KellanInquisitiveProdigyTailTheSuspect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        artifact.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Card drawn = new KellanInquisitiveProdigyTailTheSuspect();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void missingTargetDoesNotDraw() {
        addCreatureReady(player1, new KellanInquisitiveProdigyTailTheSuspect());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IzzetCluestone());
        Card undrawn = new KellanInquisitiveProdigyTailTheSuspect();
        harness.setLibrary(player1, List.of(undrawn));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerGraveyards.get(player1.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    @CardUsed({KellanInquisitiveProdigyTailTheSuspect.class, TailTheSuspect.class, Forest.class})
    void adventureAllowsExactlyTwoLandPlays() {
        harness.setHand(player1, List.of(new KellanInquisitiveProdigyTailTheSuspect(),
                new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.playLand(player1, 0)).isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Forest")).hasSize(2);
    }
}
