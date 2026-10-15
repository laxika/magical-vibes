package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartOfADuelist.class, Forest.class, GrizzlyBears.class, LlanowarElves.class,
        ThoughtReflection.class})
class HeartOfADuelistTest extends BaseCardTest {

    @Test
    void controllerChoosesAnyLibraryPositionForDraw() {
        harness.addToBattlefield(player1, new HeartOfADuelist());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new LlanowarElves()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DrawFromLibraryPositionChoice.class);
        harness.handleXValueChosen(player1, 2);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Llanowar Elves");
    }

    @Test
    void entersBattlefieldAndDrawsFromChosenPosition() {
        Forest top = new Forest();
        GrizzlyBears chosen = new GrizzlyBears();
        LlanowarElves bottom = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, chosen, bottom));
        harness.castFromHand(player1, new HeartOfADuelist(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Grizzly Bears");
    }

    @Test
    void doesNotAffectOpponentsDraws() {
        harness.addToBattlefield(player1, new HeartOfADuelist());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void canChooseTheTopCardWithoutReorderingTheLibrary() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new HeartOfADuelist());
        Forest top = new Forest();
        GrizzlyBears middle = new GrizzlyBears();
        LlanowarElves bottom = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(middle, bottom);
    }

    @Test
    void choosesSeparatelyForEachCardInAMultipleCardDraw() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new HeartOfADuelist());
        Forest top = new Forest();
        GrizzlyBears middle = new GrizzlyBears();
        LlanowarElves bottom = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DrawFromLibraryPositionChoice.class);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @CardUsed({HeartOfADuelist.class, ThoughtReflection.class, Forest.class, GrizzlyBears.class,
            LlanowarElves.class})
    void choosesSeparatelyForEachCardOfADoubledDraw() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new HeartOfADuelist());
        harness.addToBattlefield(player1, new ThoughtReflection());
        Forest top = new Forest();
        GrizzlyBears middle = new GrizzlyBears();
        LlanowarElves bottom = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DrawFromLibraryPositionChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleXValueChosen(player1, 3);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DrawFromLibraryPositionChoice.class);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
