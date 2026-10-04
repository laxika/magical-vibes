package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForebodingRuins.class, Swamp.class, Mountain.class})
class ForebodingRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Swamp or Mountain card in hand")
    void entersTappedWithoutSwampOrMountain() {
        harness.setHand(player1, List.of(new ForebodingRuins()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Swamp lets it enter untapped")
    void entersUntappedWhenRevealingSwamp() {
        harness.setHand(player1, List.of(new ForebodingRuins(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Mountain lets it enter untapped")
    void entersUntappedWhenRevealingMountain() {
        harness.setHand(player1, List.of(new ForebodingRuins(), new Mountain()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new ForebodingRuins(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new ForebodingRuins());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new ForebodingRuins());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("A matching card in the opponent's hand does not let it enter untapped")
    void opponentsHandDoesNotQualify() {
        harness.setHand(player1, List.of(new ForebodingRuins()));
        harness.setHand(player2, List.of(new Swamp()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A land without either required subtype does not qualify for reveal")
    void anotherForebodingRuinsDoesNotQualify() {
        harness.setHand(player1, List.of(new ForebodingRuins(), new ForebodingRuins()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The revealed card remains in hand")
    void revealingDoesNotConsumeTheCard() {
        Swamp swamp = new Swamp();
        harness.setHand(player1, List.of(new ForebodingRuins(), swamp));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("The controller can choose which eligible card to reveal")
    void canChooseMountainInsteadOfFirstEligibleSwamp() {
        Swamp swamp = new Swamp();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(new ForebodingRuins(), swamp, mountain));
        playLand();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp, mountain);
    }

    private Permanent findLand() {
        return findPermanent(player1, "Foreboding Ruins");
    }
}
