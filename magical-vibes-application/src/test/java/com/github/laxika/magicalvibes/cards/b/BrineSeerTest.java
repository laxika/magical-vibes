package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Donate;
import com.github.laxika.magicalvibes.cards.f.Flicker;
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

@CardUsed({BrineSeer.class, BubblingMuck.class, Donate.class, Flicker.class})
class BrineSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell using the number of selected blue cards")
    void countersSpellUsingSelectedBlueCards() {
        Permanent seer = addReadySeer();
        Donate blueCard = new Donate();
        BubblingMuck nonBlueCard = new BubblingMuck();
        harness.setHand(player1, List.of(blueCard, nonBlueCard));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 1); // only the {1}{W} casting cost

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

        harness.assertInGraveyard(player2, "Flicker");
        assertThat(gd.stack).isEmpty();
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows the spell controller to pay for the selected-card ransom")
    void spellControllerMayPayForSelectedCards() {
        Permanent seer = addReadySeer();
        Donate blueCard = new Donate();
        harness.setHand(player1, List.of(blueCard));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 2); // the casting cost plus {1} for one blue card

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(findPermanent(player1, "Brine Seer").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Charges one mana for each selected blue card")
    void chargesOneManaPerSelectedBlueCard() {
        Permanent seer = addReadySeer();
        Donate firstBlueCard = new Donate();
        Donate secondBlueCard = new Donate();
        harness.setHand(player1, List.of(firstBlueCard, secondBlueCard));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 3); // the casting cost plus {2} for two blue cards

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstBlueCard.getId(), secondBlueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(findPermanent(player1, "Brine Seer").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows revealing zero cards")
    void allowsRevealingZeroCards() {
        Permanent seer = addReadySeer();
        Donate blueCard = new Donate();
        harness.setHand(player1, List.of(blueCard));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 1);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Brine Seer").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows revealing zero cards when no blue cards are in hand")
    void allowsRevealingZeroCardsWithoutBlueCards() {
        Permanent seer = addReadySeer();
        harness.setHand(player1, List.of(new BubblingMuck()));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 1);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Brine Seer").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters the spell when its controller declines to pay")
    void countersSpellWhenPaymentIsDeclined() {
        Permanent seer = addReadySeer();
        Donate blueCard = new Donate();
        harness.setHand(player1, List.of(blueCard));
        addAbilityMana();

        Flicker spell = castFlickerAt(seer, 2); // leaves {1} available for the ransom

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Flicker");
        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-spell permanent")
    void cannotTargetNonSpellPermanent() {
        Permanent seer = addReadySeer();
        harness.setHand(player1, List.of(new Donate()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, seer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySeer() {
        return addCreatureReady(player1, new BrineSeer());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Flicker castFlickerAt(Permanent target, int genericMana) {
        Flicker spell = new Flicker();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, genericMana);
        harness.castSorcery(player2, 0, target.getId());
        harness.passPriority(player2);
        return spell;
    }
}
