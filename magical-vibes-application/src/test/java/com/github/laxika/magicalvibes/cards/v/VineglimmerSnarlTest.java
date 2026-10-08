package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VineglimmerSnarl.class, Forest.class, Island.class})
class VineglimmerSnarlTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Forest or Island card in hand")
    void entersTappedWithoutForestOrIsland() {
        harness.setHand(player1, List.of(new VineglimmerSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Forest lets it enter untapped")
    void entersUntappedWhenRevealingForest() {
        harness.setHand(player1, List.of(new VineglimmerSnarl(), new Forest()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing an Island lets it enter untapped")
    void entersUntappedWhenRevealingIsland() {
        harness.setHand(player1, List.of(new VineglimmerSnarl(), new Island()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new VineglimmerSnarl(), new Forest()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new VineglimmerSnarl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new VineglimmerSnarl());

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
    @DisplayName("Revealing a card leaves that same card in hand")
    void revealedCardRemainsInHand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new VineglimmerSnarl(), forest));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("A matching card in the opponent's hand cannot be revealed")
    void opponentHandDoesNotEnableUntappedEntry() {
        harness.setHand(player1, List.of(new VineglimmerSnarl()));
        harness.setHand(player2, List.of(new Forest(), new Island()));

        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Matching lands on the battlefield and in the graveyard do not enable untapped entry")
    void matchingCardsOutsideHandDoNotEnableUntappedEntry() {
        harness.setHand(player1, List.of(new VineglimmerSnarl()));
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Island()));

        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Vineglimmer Snarl is not a Forest or Island card")
    void anotherSnarlDoesNotEnableUntappedEntry() {
        harness.setHand(player1, List.of(new VineglimmerSnarl(), new VineglimmerSnarl()));

        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    private Permanent findLand() {
        return findPermanent(player1, "Vineglimmer Snarl");
    }
}
