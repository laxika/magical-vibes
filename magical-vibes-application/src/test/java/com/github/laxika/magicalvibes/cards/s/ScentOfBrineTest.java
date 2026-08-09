package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScentOfBrineTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell using the number of selected blue cards")
    void countersSpellUsingSelectedBlueCards() {
        ScentOfBrine scent = new ScentOfBrine();
        Counterspell blueCard = new Counterspell();
        Shock redCard = new Shock();
        harness.setHand(player1, List.of(scent, blueCard, redCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Shock spell = new Shock();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blueCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows the spell controller to pay for the selected-card ransom")
    void spellControllerMayPayForSelectedCards() {
        ScentOfBrine scent = new ScentOfBrine();
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(scent, blueCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GrizzlyBears spell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows revealing zero cards")
    void allowsRevealingZeroCards() {
        ScentOfBrine scent = new ScentOfBrine();
        harness.setHand(player1, List.of(scent, new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        GrizzlyBears spell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-spell permanent")
    void cannotTargetNonSpellPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScentOfBrine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
