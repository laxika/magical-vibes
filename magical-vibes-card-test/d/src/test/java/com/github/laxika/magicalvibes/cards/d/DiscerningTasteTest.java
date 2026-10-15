package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.n.Necrogoyf;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscerningTaste.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Island.class, LlanowarElves.class, Plains.class, Shock.class, DauthiVoidwalker.class, Necrogoyf.class})
class DiscerningTasteTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToGreatestCreaturePowerPutIntoGraveyard() {
        Card chosen = new Forest();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        Card existingGraveyardCreature = new HillGiant();
        gd.playerGraveyards.get(player1.getId()).add(existingGraveyardCreature);
        harness.setLibrary(player1, List.of(chosen, bears, elves, shock));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, elves, shock);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void gainsNoLifeWhenNoCreatureCardIsPutIntoGraveyard() {
        Card chosen = new Forest();
        Card shock = new Shock();
        Card island = new Island();
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(chosen, shock, island, plains));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, island, plains);
    }
    @Test
    void doesNotRevealLookedAtCardsToOpponent() {
        Card chosen = new Forest();
        harness.setLibrary(player1, List.of(chosen, new Island(), new Plains(), new Shock()));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(" reveals "));
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
    }

    @Test
    void gainsNoLifeForCreaturesExiledInsteadOfEnteringGraveyard() {
        harness.addToBattlefield(player2, new DauthiVoidwalker());
        Card chosen = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, creature));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void evaluatesCreaturePowerInGraveyardAfterAllCardsArePutThere() {
        Card chosen = new Forest();
        Card necrogoyf = new Necrogoyf();
        harness.setGraveyard(player2, List.of(new HillGiant(), new LlanowarElves()));
        harness.setLibrary(player1, List.of(chosen, necrogoyf, new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(necrogoyf);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void singleCardLibraryPutsCreatureIntoHandWithoutGainingLife() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void emptyLibraryResolvesWithoutGainingLife() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DiscerningTaste()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
