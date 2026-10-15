package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherSnap;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PanopticMirror.class, AetherSnap.class, EchoingTruth.class, EchoingDecay.class})
class PanopticMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Activation only offers an instant or sorcery matching X for imprint")
    void activationImprintsMatchingManaValueSpell() {
        PanopticMirror mirrorCard = new PanopticMirror();
        harness.addToBattlefield(player1, mirrorCard);
        harness.setHand(player1, List.of(new EchoingTruth(), new AetherSnap()));
        findPermanent(player1, "Panoptic Mirror").setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprintFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        Permanent mirror = findPermanent(player1, "Panoptic Mirror");
        assertThat(gd.getImprintedCard(mirror.getCard())).isSameAs(gd.getPlayerExiledCards(player1.getId()).stream()
                .filter(card -> card.getName().equals("Echoing Truth"))
                .findFirst()
                .orElseThrow());
        harness.assertInHand(player1, "Aether Snap");
    }

    @Test
    @DisplayName("Upkeep can copy and cast the imprinted card without paying its mana cost")
    void upkeepCopiesAndCastsImprintedCard() {
        PanopticMirror mirrorCard = new PanopticMirror();
        AetherSnap snapCard = new AetherSnap();
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, mirrorCard);
        gd.setImprintedCard(mirrorCard, snapCard);
        gd.exiledCards.add(new ExiledCardEntry(snapCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Aether Snap") && entry.isCopy());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Aether Snap"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Upkeep lets the controller choose among all cards imprinted on the mirror")
    void upkeepChoosesAmongMultipleImprintedCards() {
        PanopticMirror mirrorCard = new PanopticMirror();
        EchoingTruth truthCard = new EchoingTruth();
        EchoingDecay decayCard = new EchoingDecay();
        harness.addToBattlefield(player1, mirrorCard);
        harness.setHand(player1, List.of(truthCard, decayCard));
        findPermanent(player1, "Panoptic Mirror").setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ImprintFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.performUntapStep(player1);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ImprintFromHandChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Echoing Truth", "Echoing Decay");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ExiledSpellCopyChoice copyChoice =
                gd.interaction.activeInteraction(PendingInteraction.ExiledSpellCopyChoice.class);
        assertThat(copyChoice).isNotNull();
        assertThat(copyChoice.validCardIds()).containsExactlyInAnyOrder(truthCard.getId(), decayCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(truthCard.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Panoptic Mirror").getId()))
                .extracting(card -> card.getId()).containsExactly(truthCard.getId(), decayCard.getId());
    }

    @Test
    void decliningImprintStillTapsMirror() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new PanopticMirror());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(mirror.isTapped()).isTrue();
        harness.assertInHand(player1, "Echoing Truth");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void imprintRejectsArtifactWithMatchingManaValue() {
        harness.addToBattlefield(player1, new PanopticMirror());
        harness.setHand(player1, List.of(new PanopticMirror(), new AetherSnap()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class)
                .validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.assertInHand(player1, "Panoptic Mirror");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Aether Snap");
    }

    @Test
    void decliningUpkeepCopyLeavesOriginalExiled() {
        PanopticMirror mirrorCard = new PanopticMirror();
        AetherSnap snapCard = new AetherSnap();
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, mirrorCard);
        gd.setImprintedCard(mirrorCard, snapCard);
        gd.exiledCards.add(new ExiledCardEntry(snapCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(snapCard);
    }

    @Test
    void opponentUpkeepDoesNotTriggerMirror() {
        PanopticMirror mirrorCard = new PanopticMirror();
        AetherSnap snapCard = new AetherSnap();
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, mirrorCard);
        gd.setImprintedCard(mirrorCard, snapCard);
        gd.exiledCards.add(new ExiledCardEntry(snapCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(snapCard);
    }

    @Test
    void imprintStillResolvesAfterMirrorIsReturnedToHand() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new PanopticMirror());
        AetherSnap snapCard = new AetherSnap();
        harness.setHand(player1, List.of(new EchoingTruth(), snapCard));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 5, null);
        harness.castAndResolveInstant(player1, 0, mirror.getId());
        harness.assertInHand(player1, "Panoptic Mirror");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.ImprintFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(snapCard);
        harness.assertNotInHand(player1, "Aether Snap");
    }

    @Test
    void cardImprintedInResponseToUpkeepTriggerCanBeCopied() {
        harness.addToBattlefield(player1, new PanopticMirror());
        harness.setHand(player1, List.of(new AetherSnap()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Aether Snap"));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Aether Snap");
    }

    @Test
    void upkeepCopyCanChooseTargetAndReturnMirror() {
        PanopticMirror mirrorCard = new PanopticMirror();
        EchoingTruth truthCard = new EchoingTruth();
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, mirrorCard);
        gd.setImprintedCard(mirrorCard, truthCard);
        gd.exiledCards.add(new ExiledCardEntry(truthCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, mirror.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Panoptic Mirror");
        harness.assertInHand(player1, "Panoptic Mirror");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(truthCard);
        harness.assertNotInGraveyard(player1, "Echoing Truth");
    }

    @Test
    void upkeepWithoutImprintedCardsCreatesNoCopy() {
        harness.addToBattlefield(player1, new PanopticMirror());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningToCastCopyRemovesOnlyTheCopy() {
        PanopticMirror mirrorCard = new PanopticMirror();
        AetherSnap snapCard = new AetherSnap();
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, mirrorCard);
        gd.setImprintedCard(mirrorCard, snapCard);
        gd.exiledCards.add(new ExiledCardEntry(snapCard, player1.getId(), mirror.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(snapCard);
        harness.assertNotInGraveyard(player1, "Aether Snap");
    }
}
