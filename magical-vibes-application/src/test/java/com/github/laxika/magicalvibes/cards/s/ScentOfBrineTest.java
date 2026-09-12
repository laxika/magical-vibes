package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrineSeer;
import com.github.laxika.magicalvibes.cards.f.FlameJet;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
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

@CardUsed({ScentOfBrine.class, BrineSeer.class, FlameJet.class, HulkingOgre.class})
class ScentOfBrineTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell using the number of selected blue cards")
    void countersSpellUsingSelectedBlueCards() {
        ScentOfBrine scent = new ScentOfBrine();
        BrineSeer blueCard = new BrineSeer();
        FlameJet redCard = new FlameJet();
        harness.setHand(player1, List.of(scent, blueCard, redCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blueCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        harness.assertInGraveyard(player2, "Hulking Ogre");
        harness.assertInHand(player1, "Brine Seer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Allows the spell controller to pay for the selected-card ransom")
    void spellControllerMayPayForSelectedCards() {
        ScentOfBrine scent = new ScentOfBrine();
        BrineSeer blueCard = new BrineSeer();
        harness.setHand(player1, List.of(scent, blueCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hulking Ogre");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Allows revealing zero cards")
    void allowsRevealingZeroCards() {
        ScentOfBrine scent = new ScentOfBrine();
        harness.setHand(player1, List.of(scent, new BrineSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hulking Ogre");
    }

    @Test
    @DisplayName("Counters the spell when two revealed blue cards cost more than the available mana")
    void countersSpellForTwoRevealedBlueCardsWhenPaymentIsUnavailable() {
        ScentOfBrine scent = new ScentOfBrine();
        BrineSeer firstBlueCard = new BrineSeer();
        BrineSeer secondBlueCard = new BrineSeer();
        harness.setHand(player1, List.of(scent, firstBlueCard, secondBlueCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1,
                List.of(firstBlueCard.getId(), secondBlueCard.getId()));

        harness.assertInGraveyard(player2, "Hulking Ogre");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lets the spell controller decline the selected-card ransom")
    void spellControllerMayDeclineToPayForSelectedCards() {
        ScentOfBrine scent = new ScentOfBrine();
        BrineSeer blueCard = new BrineSeer();
        harness.setHand(player1, List.of(scent, blueCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hulking Ogre");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Handles a hand with no blue cards without opening a reveal choice")
    void handlesHandWithNoBlueCards() {
        ScentOfBrine scent = new ScentOfBrine();
        harness.setHand(player1, List.of(scent, new FlameJet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        HulkingOgre spell = new HulkingOgre();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hulking Ogre");
    }

    @Test
    @DisplayName("Cannot target a non-spell permanent")
    void cannotTargetNonSpellPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        harness.setHand(player1, List.of(new ScentOfBrine()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
