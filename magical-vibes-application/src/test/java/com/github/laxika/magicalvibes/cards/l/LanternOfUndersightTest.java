package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.t.ThoughtReflection;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternOfUndersight.class, Forest.class, GrizzlyBears.class,
        ThoughtReflection.class, SongOfTheDryads.class})
class LanternOfUndersightTest extends BaseCardTest {

    @Test
    void controllerDrawsFromBottomOfLibrary() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void doesNotAffectOpponentsDraws() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void multipleCardsAreDrawnFromBottomInOrder() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        Forest top = new Forest();
        GrizzlyBears middle = new GrizzlyBears();
        Forest bottom = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void doubledDrawsStillComeFromBottom() {
        harness.addToBattlefield(player1, new LanternOfUndersight());
        harness.addToBattlefield(player1, new ThoughtReflection());
        Forest top = new Forest();
        GrizzlyBears middle = new GrizzlyBears();
        Forest bottom = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top, middle, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bottom, middle);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void drawsFromTopAfterLanternBecomesForest() {
        var lantern = harness.addToBattlefieldAndReturn(player1, new LanternOfUndersight());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, lantern.getId());
        harness.passBothPriorities();
        Forest top = new Forest();
        GrizzlyBears bottom = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(top, bottom));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
    }
}
