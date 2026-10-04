package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IxallisDiviner;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeraldsReveille.class, Forest.class, IxallisDiviner.class, MerfolkOfThePearlTrident.class})
class HeraldsReveilleTest extends BaseCardTest {

    @Test
    void drawsNormallyWhenNoPermanentExplored() {
        Card drawn = new Forest();
        harness.setHand(player1, List.of(new HeraldsReveille()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void seeksAMerfolkInsteadAfterAPermanentExplored() {
        Forest exploredLand = new Forest();
        Card sought = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(exploredLand, sought));
        harness.setHand(player1, List.of(new IxallisDiviner(), new HeraldsReveille()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(exploredLand.getId(), sought.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenExploredButNoMerfolkCanBeSought() {
        Forest exploredLand = new Forest();
        Forest remainingLand = new Forest();
        harness.setLibrary(player1, List.of(exploredLand, remainingLand));
        harness.setHand(player1, List.of(new IxallisDiviner(), new HeraldsReveille()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(exploredLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
    }

    @Test
    void seeksBelowTheTopCardEvenAfterExploringPermanentLeaves() {
        Forest exploredLand = new Forest();
        Forest topLand = new Forest();
        Forest bottomLand = new Forest();
        Card sought = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(exploredLand, topLand, sought, bottomLand));
        harness.setHand(player1, List.of(new IxallisDiviner(), new HeraldsReveille()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        var diviner = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, diviner));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(exploredLand, sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topLand, bottomLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void nonlandExploreAlsoEnablesSeeking() {
        Card sought = new MerfolkOfThePearlTrident();
        Forest remainingLand = new Forest();
        harness.setLibrary(player1, List.of(sought, remainingLand));
        harness.setHand(player1, List.of(new IxallisDiviner(), new HeraldsReveille()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
    }

    @Test
    void opponentsExploreDoesNotReplaceTheDraw() {
        harness.forceActivePlayer(player2);
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new IxallisDiviner()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        Forest drawn = new Forest();
        Card merfolk = new MerfolkOfThePearlTrident();
        harness.setLibrary(player1, List.of(drawn, merfolk));
        harness.setHand(player1, List.of(new HeraldsReveille()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(merfolk);
    }
}
