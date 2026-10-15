package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({ChokedEstuary.class, Forest.class, Island.class, Swamp.class})
class ChokedEstuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Island or Swamp card in hand")
    void entersTappedWithoutIslandOrSwamp() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Forest()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing an Island lets it enter untapped")
    void entersUntappedWhenRevealingIsland() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Swamp lets it enter untapped")
    void entersUntappedWhenRevealingSwamp() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new ChokedEstuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new ChokedEstuary());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped with no other cards in hand")
    void entersTappedWithEmptyHand() {
        harness.setHand(player1, List.of(new ChokedEstuary()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Island on the battlefield cannot be revealed from hand")
    void battlefieldIslandDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new ChokedEstuary()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Swamp in hand cannot be revealed")
    void opponentsHandDoesNotAllowUntappedEntry() {
        harness.setHand(player2, List.of(new Swamp()));
        harness.setHand(player1, List.of(new ChokedEstuary()));
        playLand();

        assertThat(findLand(player1).isTapped()).isTrue();
        harness.assertInHand(player2, "Swamp");
    }

    @Test
    @DisplayName("Revealing a card leaves it in hand and allows immediate mana production")
    void revealedCardStaysInHand() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller chooses which eligible land card to reveal")
    void controllerChoosesCardToReveal() {
        harness.setHand(player1, List.of(new ChokedEstuary(), new Island(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerHands.get(player1.getId()).get(1).getId()));

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gameLogContains("reveals Swamp")).isTrue();
        assertThat(gameLogContains("reveals Island")).isFalse();
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Swamp");
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Choked Estuary");
    }
}
