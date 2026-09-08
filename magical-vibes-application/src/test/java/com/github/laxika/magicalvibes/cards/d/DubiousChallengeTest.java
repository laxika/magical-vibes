package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DubiousChallengeTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent takes one exiled creature and the controller gets the rest")
    void opponentTakesOneAndControllerGetsTheRest() {
        Card opponentCreature = new GrizzlyBears();
        Card controllerCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                opponentCreature, new Shock(), new Forest(), controllerCreature,
                new Shock(), new Forest(), new Shock(), new Forest(), new Shock(), new Forest()));

        castDubiousChallenge();

        PendingInteraction.LibraryRevealChoice controllerChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(controllerChoice.playerId()).isEqualTo(player1.getId());
        assertThat(controllerChoice.validCardIds()).containsExactlyInAnyOrder(
                opponentCreature.getId(), controllerCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(
                opponentCreature.getId(), controllerCreature.getId()));

        PendingInteraction.LibraryRevealChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player2.getId());
        assertThat(opponentChoice.validCardIds()).containsExactlyInAnyOrder(
                opponentCreature.getId(), controllerCreature.getId());

        harness.handleMultipleCardsChosen(player2, List.of(opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(controllerCreature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8)
                .doesNotContain(opponentCreature, controllerCreature);
    }

    @Test
    @DisplayName("Declining the opponent's choice puts every exiled creature under the controller's control")
    void opponentDeclinesAndControllerGetsEveryExiledCreature() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(
                firstCreature, secondCreature, new Shock(), new Forest(), new Shock(),
                new Forest(), new Shock(), new Forest(), new Shock(), new Forest()));

        castDubiousChallenge();
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstCreature.getId(), secondCreature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(firstCreature.getId())
                        || permanent.getCard().getId().equals(secondCreature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A top ten with no creature cards is shuffled without opening a choice")
    void noCreatureCardsMeansNoChoice() {
        List<Card> library = List.of(
                new Shock(), new Forest(), new Shock(), new Forest(), new Shock(),
                new Forest(), new Shock(), new Forest(), new Shock(), new Forest());
        harness.setLibrary(player1, library);

        castDubiousChallenge();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new DubiousChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDubiousChallenge() {
        harness.setHand(player1, List.of(new DubiousChallenge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
