package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeartOfADuelist.class, Forest.class, GrizzlyBears.class, LlanowarElves.class})
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
        harness.setHand(player1, List.of(new HeartOfADuelist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
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
}
