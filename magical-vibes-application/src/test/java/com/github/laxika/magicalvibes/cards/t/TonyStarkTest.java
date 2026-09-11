package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SHIELDSpyKit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TonyStark.class, TheInvincibleIronMan.class, TheMindStone.class, SHIELDSpyKit.class, Shock.class})
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
        List<Card> topCards = List.of(artifact, new Shock(), new Shock(), new Shock());
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

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(artifact.getId())));

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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent battlefieldPermanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
