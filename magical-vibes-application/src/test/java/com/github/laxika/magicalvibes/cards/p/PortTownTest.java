package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PortTown.class, Plains.class, Island.class, Forest.class})
class PortTownTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Plains or Island card in hand")
    void entersTappedWithoutPlainsOrIsland() {
        harness.setHand(player1, List.of(new PortTown(), new Forest()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Plains lets it enter untapped")
    void entersUntappedWhenRevealingPlains() {
        harness.setHand(player1, List.of(new PortTown(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing an Island lets it enter untapped")
    void entersUntappedWhenRevealingIsland() {
        harness.setHand(player1, List.of(new PortTown(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new PortTown(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new PortTown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new PortTown());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("Enters tapped when playing it leaves an empty hand")
    void entersTappedWithEmptyHand() {
        harness.setHand(player1, List.of(new PortTown()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Plains in an opponent's hand cannot be revealed")
    void opponentsPlainsDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new PortTown()));
        harness.setHand(player2, List.of(new Plains()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Another Port Town is not a Plains or Island")
    void anotherPortTownCannotBeRevealed() {
        harness.setHand(player1, List.of(new PortTown(), new PortTown()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Revealing leaves the card in hand and allows immediate mana production")
    void revealedCardStaysInHandAndLandCanTapImmediately() {
        Plains plains = new Plains();
        harness.setHand(player1, List.of(new PortTown(), plains));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses which eligible card to reveal")
    void multipleEligibleCardsRequireARevealChoice() {
        harness.setHand(player1, List.of(new PortTown(), new Plains(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gameLogContains("reveals Plains")).isFalse();
        assertThat(gameLogContains("reveals Island")).isFalse();
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Port Town");
    }
}
