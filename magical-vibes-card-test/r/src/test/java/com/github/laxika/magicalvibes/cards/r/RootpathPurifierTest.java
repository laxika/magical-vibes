package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootpathPurifier.class, RootboundCrag.class, Cultivate.class, GrizzlyBears.class})
class RootpathPurifierTest extends BaseCardTest {

    @Test
    void controlledLandsBecomeBasicButOpponentsLandsDoNot() {
        harness.addToBattlefield(player1, new RootpathPurifier());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new RootboundCrag());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new RootboundCrag());

        assertThat(gqs.hasEffectiveSupertype(gd, ownLand, CardSupertype.BASIC)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, opponentLand, CardSupertype.BASIC)).isFalse();
    }

    @Test
    void landCardsInLibraryBecomeBasicForSearches() {
        harness.addToBattlefield(player1, new RootpathPurifier());
        RootboundCrag libraryLand = new RootboundCrag();
        RootboundCrag handLand = new RootboundCrag();
        harness.setLibrary(player1, List.of(libraryLand, new GrizzlyBears()));
        harness.setHand(player1, List.of(new Cultivate(), handLand));

        assertThat(gqs.cardHasSupertype(libraryLand, CardSupertype.BASIC, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSupertype(handLand, CardSupertype.BASIC, gd, player1.getId())).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(libraryLand);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getName))
                .contains("Rootbound Crag");
    }
}
