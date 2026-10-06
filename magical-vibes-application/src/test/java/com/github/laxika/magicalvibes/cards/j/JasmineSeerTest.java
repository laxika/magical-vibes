package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.ScentOfBrine;
import com.github.laxika.magicalvibes.cards.s.ScentOfCinder;
import com.github.laxika.magicalvibes.cards.s.SerraAdvocate;
import com.github.laxika.magicalvibes.cards.v.VoiceOfDuty;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasmineSeer.class, VoiceOfDuty.class, SerraAdvocate.class, ScentOfCinder.class,
        ScentOfBrine.class})
class JasmineSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each white card chosen to reveal")
    void gainsLifeForWhiteCardsInHand() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        VoiceOfDuty firstWhiteCard = new VoiceOfDuty();
        SerraAdvocate secondWhiteCard = new SerraAdvocate();
        harness.setHand(player1, List.of(firstWhiteCard, secondWhiteCard, new ScentOfCinder()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId(), secondWhiteCard.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores nonwhite cards in hand")
    void ignoresNonwhiteCards() {
        addCreatureReady(player1, new JasmineSeer());
        harness.setHand(player1, List.of(new ScentOfCinder(), new ScentOfBrine()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Counts only white cards in the controller's hand")
    void countsOnlyWhiteCardsInControllerHand() {
        addCreatureReady(player1, new JasmineSeer());
        VoiceOfDuty whiteCard = new VoiceOfDuty();
        harness.setHand(player1, List.of(whiteCard, new ScentOfCinder()));
        harness.setHand(player2, List.of(new VoiceOfDuty(), new SerraAdvocate()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new JasmineSeer());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Chooses only some white cards to reveal and gains life for those cards")
    void gainsLifeOnlyForChosenCards() {
        addCreatureReady(player1, new JasmineSeer());
        VoiceOfDuty revealedCard = new VoiceOfDuty();
        SerraAdvocate hiddenCard = new SerraAdvocate();
        ScentOfCinder redCard = new ScentOfCinder();
        harness.setHand(player1, List.of(revealedCard, hiddenCard, redCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(revealedCard.getId(), hiddenCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(revealedCard.getId()));

        harness.assertLife(player1, 22);
        assertThat(gameLogContains("reveals Voice of Duty")).isTrue();
        assertThat(gameLogContains("Serra Advocate")).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealedCard, hiddenCard, redCard);
    }

    @Test
    @DisplayName("Can reveal zero cards despite having white cards in hand")
    void canRevealZeroCards() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        VoiceOfDuty whiteCard = new VoiceOfDuty();
        harness.setHand(player1, List.of(whiteCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player1, 20);
        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard);
    }

    @Test
    @DisplayName("Can activate with an empty hand and gains no life")
    void canActivateWithEmptyHand() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        harness.setHand(player1, List.of());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        seer.tap();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent seer = addCreatureReady(player1, new JasmineSeer());
        seer.setSummoningSick(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
