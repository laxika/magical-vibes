package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({NecroblossomSnarl.class, Forest.class, Swamp.class, Plains.class})
class NecroblossomSnarlTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Swamp or Forest card in hand")
    void entersTappedWithoutSwampOrForest() {
        harness.setHand(player1, List.of(new NecroblossomSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Swamp lets it enter untapped")
    void entersUntappedWhenRevealingSwamp() {
        harness.setHand(player1, List.of(new NecroblossomSnarl(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Forest lets it enter untapped")
    void entersUntappedWhenRevealingForest() {
        harness.setHand(player1, List.of(new NecroblossomSnarl(), new Forest()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new NecroblossomSnarl(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A land without the required subtypes cannot be revealed")
    void entersTappedWithOnlyPlainsInHand() {
        harness.setHand(player1, List.of(new NecroblossomSnarl(), new Plains()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("An opponent's Swamp does not satisfy the reveal condition")
    void entersTappedWithMatchingCardOnlyInOpponentsHand() {
        harness.setHand(player1, List.of(new NecroblossomSnarl()));
        harness.setHand(player2, List.of(new Swamp()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a card keeps that same card in hand")
    void revealedCardRemainsInHand() {
        Swamp swamp = new Swamp();
        harness.setHand(player1, List.of(new NecroblossomSnarl(), swamp));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("A Swamp on the battlefield does not satisfy the reveal condition")
    void entersTappedWithMatchingCardOnlyOnBattlefield() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new NecroblossomSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new NecroblossomSnarl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new NecroblossomSnarl());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findLand() {
        return findPermanent(player1, "Necroblossom Snarl");
    }
}
