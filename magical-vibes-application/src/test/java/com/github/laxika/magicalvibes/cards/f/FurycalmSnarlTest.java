package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurycalmSnarl.class, Mountain.class, Plains.class, Forest.class})
class FurycalmSnarlTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Mountain or Plains card in hand")
    void entersTappedWithoutMountainOrPlains() {
        harness.setHand(player1, List.of(new FurycalmSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Mountain lets it enter untapped")
    void entersUntappedWhenRevealingMountain() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Mountain()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Plains lets it enter untapped")
    void entersUntappedWhenRevealingPlains() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Mountain()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Forest in hand cannot be revealed instead of a Mountain or Plains")
    void entersTappedWithOnlyForestInHand() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Forest()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Another Furycalm Snarl is not a Mountain or Plains card")
    void entersTappedWithAnotherSnarlInHand() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new FurycalmSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A Mountain on the battlefield does not satisfy the reveal condition")
    void entersTappedWithMountainOnlyOnBattlefield() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new FurycalmSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Plains in the opponent's hand cannot be revealed")
    void entersTappedWithPlainsOnlyInOpponentsHand() {
        harness.setHand(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new FurycalmSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a matching card leaves it in hand")
    void revealingDoesNotConsumeTheCard() {
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Forest(), mountain));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(mountain).hasSize(2);
        assertThat(gameLogContains("reveals Mountain")).isTrue();
    }

    @Test
    @DisplayName("The controller chooses which eligible card to reveal")
    void acceptingRevealOffersChoiceBetweenEligibleCards() {
        harness.setHand(player1, List.of(new FurycalmSnarl(), new Mountain(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gameLogContains("reveals Mountain")).isFalse();
        assertThat(gameLogContains("reveals Plains")).isFalse();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addCreatureReady(player1, new FurycalmSnarl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addCreatureReady(player1, new FurycalmSnarl());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findLand() {
        return findPermanent(player1, "Furycalm Snarl");
    }
}
