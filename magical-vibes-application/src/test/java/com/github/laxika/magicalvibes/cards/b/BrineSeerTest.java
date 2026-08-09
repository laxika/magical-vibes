package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrineSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell using the number of selected blue cards")
    void countersSpellUsingSelectedBlueCards() {
        Permanent seer = addReadySeer();
        Counterspell blueCard = new Counterspell();
        Shock redCard = new Shock();
        harness.setHand(player1, List.of(blueCard, redCard));
        addAbilityMana();

        Shock spell = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blueCard.getId());
        assertThat(choice.legalOptions()).isEqualTo(
                new com.github.laxika.magicalvibes.model.InteractionOptions.MultiCardPick(
                        List.of(blueCard.getId()), 0, 1));

        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows the spell controller to pay for the selected-card ransom")
    void spellControllerMayPayForSelectedCards() {
        addReadySeer();
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(blueCard));
        addAbilityMana();

        Shock spell = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows revealing zero cards")
    void allowsRevealingZeroCards() {
        addReadySeer();
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(blueCard));
        addAbilityMana();

        Shock spell = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-spell permanent")
    void cannotTargetNonSpellPermanent() {
        Permanent seer = addReadySeer();
        harness.setHand(player1, List.of(new Counterspell()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, seer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeer() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new BrineSeer());
        seer.setSummoningSick(false);
        return seer;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
