package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FortifiedVillage.class, Forest.class, Plains.class})
class FortifiedVillageTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Forest or Plains card in hand")
    void entersTappedWithoutForestOrPlains() {
        harness.setHand(player1, List.of(new FortifiedVillage()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Forest lets it enter untapped")
    void entersUntappedWhenRevealingForest() {
        harness.setHand(player1, List.of(new FortifiedVillage(), new Forest()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Plains lets it enter untapped")
    void entersUntappedWhenRevealingPlains() {
        harness.setHand(player1, List.of(new FortifiedVillage(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new FortifiedVillage(), new Forest()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new FortifiedVillage());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new FortifiedVillage());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("Revealing keeps the matching card in hand and makes mana available immediately")
    void revealedCardStaysInHand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new FortifiedVillage(), forest));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Matching cards outside your hand cannot be revealed")
    void matchingCardsOutsideYourHandDoNotAllowUntappedEntry() {
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setHand(player2, List.of(new Forest(), new Plains()));
        harness.setHand(player1, List.of(new FortifiedVillage()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent findLand() {
        return findPermanent(player1, "Fortified Village");
    }
}
