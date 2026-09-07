package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissaResurgentAnimist.class, Forest.class, GrizzlyBears.class,
        LlanowarElves.class, ElvishMystic.class})
class NissaResurgentAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall uses the stack and adds one mana of a chosen color")
    void landfallUsesStackAndAddsMana() {
        harness.addToBattlefield(player1, new NissaResurgentAnimist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second resolved landfall ability reveals to the first Elf or Elemental")
    void secondResolutionFindsElfOrElemental() {
        harness.addToBattlefield(player1, new NissaResurgentAnimist());
        Card land = new Forest();
        Card nonMatch = new GrizzlyBears();
        Card found = new LlanowarElves();
        harness.setLibrary(player1, List.of(nonMatch, found));
        gd.playerHands.get(player1.getId()).clear();

        resolveLandfall(land);
        resolveLandfall(new Forest());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonMatch);
    }

    @Test
    @DisplayName("Landfall after the second resolution only adds mana")
    void laterResolutionsDoNotRevealAgain() {
        harness.addToBattlefield(player1, new NissaResurgentAnimist());
        Card firstFound = new LlanowarElves();
        Card secondFound = new ElvishMystic();
        harness.setLibrary(player1, List.of(firstFound, secondFound));
        gd.playerHands.get(player1.getId()).clear();

        resolveLandfall(new Forest());
        resolveLandfall(new Forest());
        resolveLandfall(new Forest());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstFound);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondFound);
    }

    @Test
    @DisplayName("The second resolution randomizes the entire library when no Elf or Elemental is found")
    void secondResolutionWithNoMatchLeavesLibraryIntact() {
        harness.addToBattlefield(player1, new NissaResurgentAnimist());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        gd.playerHands.get(player1.getId()).clear();

        resolveLandfall(new Forest());
        resolveLandfall(new Forest());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    private void resolveLandfall(Card land) {
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        gd.playerHands.get(player1.getId()).add(land);
        harness.playLand(player1, gd.playerHands.get(player1.getId()).size() - 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ManaColor.RED.name());
    }
}
