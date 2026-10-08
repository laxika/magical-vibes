package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SHIELDSpyKit;
import com.github.laxika.magicalvibes.cards.i.IAmIronMan;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TonyStark.class, TheInvincibleIronMan.class, TheMindStone.class, SHIELDSpyKit.class, IAmIronMan.class})
class TonyStarkTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using either face")
    void castsUsingEitherFace() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new TonyStark(), new TonyStark()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Looks at four cards and may put an artifact into hand")
    void looksAtTopFourCards() {
        Permanent tony = addFrontReady();
        TheMindStone artifact = new TheMindStone();
        List<Card> topCards = List.of(artifact, new IAmIronMan(), new IAmIronMan(), new IAmIronMan());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, topCards);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(topCards.subList(1, 4));
        assertThat(tony.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts an artifact from hand onto the battlefield and attaches an Equipment")
    void putsArtifactFromHandAndAttachesEquipment() {
        Permanent ironMan = addBackReady();
        SHIELDSpyKit equipment = new SHIELDSpyKit();
        harness.setHand(player1, List.of(equipment));
        advanceToBeginningOfCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredEquipment = battlefieldPermanentFor(equipment);
        assertThat(enteredEquipment.getAttachedTo()).isEqualTo(ironMan.getId());
    }

    @Test
    @DisplayName("Does not attach a non-Equipment artifact put onto the battlefield")
    void doesNotAttachNonEquipmentArtifact() {
        Permanent ironMan = addBackReady();
        TheMindStone artifact = new TheMindStone();
        harness.setHand(player1, List.of(artifact));
        advanceToBeginningOfCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredArtifact = battlefieldPermanentFor(artifact);
        assertThat(enteredArtifact.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ironMan, enteredArtifact);
    }

    @Test
    void transformsWithoutUntappingOrReplacingThePermanent() {
        Permanent tony = addFrontReady();
        tony.tap();
        tony.setMarkedDamage(1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(tony);
        assertThat(tony.isTransformed()).isTrue();
        assertThat(tony.getCard()).isInstanceOf(TheInvincibleIronMan.class);
        assertThat(tony.isTapped()).isTrue();
        assertThat(tony.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTransformDuringCombat() {
        Permanent tony = addFrontReady();
        prepareMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tony.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineArtifactEvenWithOnlyOneCardInLibrary() {
        addFrontReady();
        TheMindStone artifact = new TheMindStone();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(artifact));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
    }

    @Test
    void leavesCardsBelowTopFourAboveTheReturnedCards() {
        addFrontReady();
        List<Card> topCards = List.of(new IAmIronMan(), new IAmIronMan(),
                new IAmIronMan(), new IAmIronMan());
        TheMindStone fifthCard = new TheMindStone();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCards.get(0), topCards.get(1),
                topCards.get(2), topCards.get(3), fifthCard));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(topCards);
    }

    @Test
    void mayDeclinePuttingArtifactOntoBattlefield() {
        Permanent ironMan = addBackReady();
        SHIELDSpyKit equipment = new SHIELDSpyKit();
        harness.setHand(player1, List.of(equipment));
        advanceToBeginningOfCombat();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ironMan);
    }

    private Permanent addFrontReady() {
        return addCreatureReady(player1, new TonyStark());
    }

    private Permanent addBackReady() {
        TonyStark card = new TonyStark();
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToBeginningOfCombat() {
        prepareMainPhase();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    private Permanent battlefieldPermanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
