package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightshadeSeer.class, HulkingOgre.class, RavenousRats.class, ThranDynamo.class})
class NightshadeSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X for the number of selected black cards")
    void givesTargetCreatureMinusForSelectedBlackCards() {
        Permanent seer = addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats blackCard = new RavenousRats();
        HulkingOgre nonBlackCard = new HulkingOgre();
        harness.setHand(player1, List.of(blackCard, nonBlackCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blackCard, nonBlackCard);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Allows revealing zero black cards")
    void allowsRevealingZeroBlackCards() {
        addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        harness.setHand(player1, List.of(new HulkingOgre()));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows choosing zero even when black cards are available")
    void allowsChoosingZeroFromAvailableBlackCards() {
        Permanent seer = addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats blackCard = new RavenousRats();
        harness.setHand(player1, List.of(blackCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(blackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Uses only the black cards actually selected when several are available")
    void usesOnlyActuallySelectedBlackCards() {
        Permanent seer = addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats firstBlackCard = new RavenousRats();
        RavenousRats secondBlackCard = new RavenousRats();
        harness.setHand(player1, List.of(firstBlackCard, secondBlackCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(firstBlackCard.getId(), secondBlackCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(2);
        assertThat(seer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The temporary penalty wears off at end of turn")
    void penaltyWearsOffAtEndOfTurn() {
        Permanent seer = addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats blackCard = new RavenousRats();
        harness.setHand(player1, List.of(blackCard));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(blackCard.getId()));

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadySeer();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2,
                new ThranDynamo());
        harness.setHand(player1, List.of(new RavenousRats()));
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revealing several black cards can reduce a creature to zero toughness")
    void multipleRevealedCardsKillTarget() {
        addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats first = new RavenousRats();
        RavenousRats second = new RavenousRats();
        NightshadeSeer third = new NightshadeSeer();
        harness.setHand(player1, List.of(first, second, third));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        harness.assertNotOnBattlefield(player2, "Hulking Ogre");
        harness.assertInGraveyard(player2, "Hulking Ogre");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
    }

    @Test
    @DisplayName("Chooses cards from the hand at resolution rather than activation")
    void usesHandAtResolution() {
        addReadySeer();
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new HulkingOgre());
        RavenousRats original = new RavenousRats();
        NightshadeSeer replacement = new NightshadeSeer();
        harness.setHand(player1, List.of(original));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, ogre.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.setHand(player1, List.of(replacement));
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice)
                        gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(replacement.getId());
        harness.handleMultipleCardsChosen(player1, List.of(replacement.getId()));

        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(replacement);
    }

    private Permanent addReadySeer() {
        return addCreatureReady(player1, new NightshadeSeer());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
