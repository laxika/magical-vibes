package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackAssembler;
import com.github.laxika.magicalvibes.model.GameStatus;
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

    @Test
    void maxSpeedRequiresBothModes() {
        gd.playerSpeeds.put(player1.getId(), 4);
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentMaxSpeedDoesNotAllowBothModes() {
        gd.playerSpeeds.put(player2.getId(), 4);
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void missingKeywordMatchDoesNotPreventSecondSeek() {
        gd.playerSpeeds.put(player1.getId(), 4);
        GrizzlyBears bear = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, bear));

        castWithModes(0, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void firstSeekCannotSeekTheSameCardAgain() {
        gd.playerSpeeds.put(player1.getId(), 4);
        HangarbackAssembler artifact = new HangarbackAssembler();
        harness.setLibrary(player1, List.of(artifact));

        castWithModes(0, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Surge of Acclaim");
    }

    @Test
    void nonlandSeekWithoutAMatchLeavesLibraryOrderUnchanged() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        castWithModes(1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Surge of Acclaim");
    }

    @Test
    void seekDoesNotPubliclyRevealTheSelectedCard() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castWithModes(1);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("Grizzly Bears"));
    }

    @Test
    void seekingFromAnEmptyLibraryDoesNotLoseTheGame() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setLibrary(player1, List.of());

        castWithModes(0, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertInGraveyard(player1, "Surge of Acclaim");
    }

    @Test
    void successfulSeekPreservesTheOrderOfOtherCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, bear, third));

        castWithModes(1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    void jumpStartUsesModeChosenAtCastingAndExilesTheSpell() {
        SurgeOfAcclaim spell = new SurgeOfAcclaim();
        Forest discard = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(bear));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, -2, null, List.of(), null, null, List.of(), 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
        assertThat(gd.stack).isEmpty();
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
