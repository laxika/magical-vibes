package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({ShineshadowSnarl.class, Forest.class, Plains.class, Swamp.class})
class ShineshadowSnarlTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Plains or Swamp card in hand")
    void entersTappedWithoutPlainsOrSwamp() {
        harness.setHand(player1, List.of(new ShineshadowSnarl(), new Forest()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Plains lets it enter untapped")
    void entersUntappedWhenRevealingPlains() {
        harness.setHand(player1, List.of(new ShineshadowSnarl(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Swamp lets it enter untapped")
    void entersUntappedWhenRevealingSwamp() {
        harness.setHand(player1, List.of(new ShineshadowSnarl(), new Swamp()));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new ShineshadowSnarl(), new Plains()));
        playLand();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addCreatureReady(player1, new ShineshadowSnarl());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addCreatureReady(player1, new ShineshadowSnarl());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when playing it leaves an empty hand")
    void entersTappedWithEmptyHand() {
        harness.setHand(player1, List.of(new ShineshadowSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Plains on the battlefield and a Swamp in an opponent's hand cannot be revealed")
    void matchingCardsOutsideYourHandDoNotHelp() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player2, List.of(new Swamp()));
        harness.setHand(player1, List.of(new ShineshadowSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Snarl is not a Plains or Swamp card")
    void anotherSnarlCannotBeRevealed() {
        harness.setHand(player1, List.of(new ShineshadowSnarl(), new ShineshadowSnarl()));
        playLand();

        assertThat(findLand().isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Revealing a card keeps that same card in hand")
    void revealedCardStaysInHand() {
        Plains plains = new Plains();
        harness.setHand(player1, List.of(new ShineshadowSnarl(), plains));
        playLand();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand().isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findLand() {
        return findPermanent(player1, "Shineshadow Snarl");
    }
}
