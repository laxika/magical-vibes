package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackAssembler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgeOfAcclaim.class, HangarbackAssembler.class, GrizzlyBears.class, Forest.class})
class SurgeOfAcclaimTest extends BaseCardTest {

    @Test
    void seeksStartYourEnginesKeywordCardIntoHand() {
        harness.setLibrary(player1, List.of(new HangarbackAssembler(), new Forest()));
        castWithModes(0);

        harness.assertInHand(player1, "Hangarback Assembler");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
    }

    @Test
    void seeksNonlandCardIntoHand() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        castWithModes(1);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void maxSpeedSeeksBothCards() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of(new HangarbackAssembler(), new GrizzlyBears(), new Forest()));
        castWithModes(0, 1);

        harness.assertInHand(player1, "Hangarback Assembler");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void cannotChooseBothBelowMaxSpeed() {
        harness.setLibrary(player1, List.of(new HangarbackAssembler(), new GrizzlyBears()));
        assertThatThrownBy(() -> {
            prepareSpell();
            harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        }).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional modal modes");
    }

    private void castWithModes(int... modeIndices) {
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modeIndices, List.of());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new SurgeOfAcclaim()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
