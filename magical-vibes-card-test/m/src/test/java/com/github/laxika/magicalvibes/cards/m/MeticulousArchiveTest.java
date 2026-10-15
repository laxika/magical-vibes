package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeticulousArchive.class, NoviceInspector.class})
class MeticulousArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new NoviceInspector();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MeticulousArchive()));

        harness.playLand(player1, 0);
        Permanent archive = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(archive.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Surveil can leave the card on top without changing library order")
    void canKeepTopCard() {
        Card topCard = new NoviceInspector();
        Card nextCard = new MeticulousArchive();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new MeticulousArchive()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil resolves with an empty library without requesting a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MeticulousArchive()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meticulous Archive");
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        Permanent archive = addReadyArchive();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        Permanent archive = addReadyArchive();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyArchive() {
        return harness.addToBattlefieldAndReturn(player1, new MeticulousArchive());
    }
}
