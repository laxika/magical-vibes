package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlmostPerfect;
import com.github.laxika.magicalvibes.cards.i.InfestingRadroach;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vault13DwellersJourney.class, InfestingRadroach.class, IntangibleVirtue.class, AlmostPerfect.class})
class Vault13DwellersJourneyTest extends BaseCardTest {

    @Test
    void chapterIExilesAtMostOneCreaturePerPlayer() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new InfestingRadroach());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();

        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(ownCreature.getId(), opponentCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(saga.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(ownCreature.getCard().getId(), opponentCreature.getCard().getId());
    }

    @Test
    void chapterIIGainsLifeAndScriesTwo() {
        harness.setLibrary(player1, List.of(new InfestingRadroach(), new InfestingRadroach(), new InfestingRadroach()));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();
    }

    @Test
    void chapterIIIReturnsTwoToOwnersAndBottomsTheRest() {
        Permanent saga = addSagaWithLore(2);
        InfestingRadroach ownReturned = new InfestingRadroach();
        InfestingRadroach opponentReturned = new InfestingRadroach();
        InfestingRadroach ownBottom = new InfestingRadroach();
        gd.addToExile(player1.getId(), ownReturned, saga.getId());
        gd.addToExile(player2.getId(), opponentReturned, saga.getId());
        gd.addToExile(player1.getId(), ownBottom, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1,
                List.of(ownReturned.getId(), opponentReturned.getId()));
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player1, ownReturned.getId())).isNotNull();
        assertThat(findCardOnBattlefield(player2, opponentReturned.getId())).isNotNull();
        assertThat(findCardOnBattlefield(player1, ownBottom.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getId())
                .contains(ownBottom.getId());
    }

    @Test
    void chapterITargetsEnchantmentsAndPreventsASecondTargetFromTheSamePlayer() {
        addSagaWithLore(0);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new IntangibleVirtue());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new InfestingRadroach());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantment.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(enchantment.getCard().getId(), opponentCreature.getCard().getId());
        assertThat(findCardOnBattlefield(player1, ownCreature.getCard().getId())).isNotNull();
    }

    @Test
    void chapterICanChooseNoTargets() {
        addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(findCardOnBattlefield(player2, creature.getCard().getId())).isNotNull();
    }

    @Test
    void chapterIDoesNotOfferOpposingHexproofOrShroudPermanents() {
        addSagaWithLore(0);
        Permanent ownHexproof = harness.addToBattlefieldAndReturn(player1, new InfestingRadroach());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());
        Permanent shroud = harness.addToBattlefieldAndReturn(player1, new InfestingRadroach());
        ownHexproof.getGrantedKeywords().add(Keyword.HEXPROOF);
        opponentHexproof.getGrantedKeywords().add(Keyword.HEXPROOF);
        shroud.getGrantedKeywords().add(Keyword.SHROUD);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownHexproof.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentHexproof.getId(), shroud.getId());
    }

    @Test
    void exiledCreatureReturnsToItsOwnerWhenSagaLeavesEarly() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(findCardOnBattlefield(player2, creature.getCard().getId())).isNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));

        assertThat(findCardOnBattlefield(player2, creature.getCard().getId())).isNotNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void chapterIDoesNotExileIfSagaLeavesBeforeResolution() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player2, creature.getCard().getId())).isNotNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void chapterIIIReturnsTheOnlyExiledCardAndSacrificesSaga() {
        Permanent saga = addSagaWithLore(2);
        InfestingRadroach creature = new InfestingRadroach();
        gd.addToExile(player2.getId(), creature, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player2, creature.getId())).isNotNull();
        assertThat(findCardOnBattlefield(player1, saga.getCard().getId())).isNull();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void chapterIIIWithNoExiledCardsStillSacrificesSaga() {
        Permanent saga = addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player1, saga.getCard().getId())).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIILetsTheOwnerOrderMultipleCardsGoingToTheirLibrary() {
        Permanent saga = addSagaWithLore(2);
        InfestingRadroach ownReturned = new InfestingRadroach();
        InfestingRadroach opponentReturned = new InfestingRadroach();
        InfestingRadroach bottomCreature = new InfestingRadroach();
        IntangibleVirtue bottomEnchantment = new IntangibleVirtue();
        gd.addToExile(player1.getId(), ownReturned, saga.getId());
        gd.addToExile(player2.getId(), opponentReturned, saga.getId());
        gd.addToExile(player2.getId(), bottomCreature, saga.getId());
        gd.addToExile(player2.getId(), bottomEnchantment, saga.getId());

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(ownReturned.getId(), opponentReturned.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    void chapterIIIReturnsTwoActuallyExiledCreaturesWithoutReturningThemAgainOnSacrifice() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new InfestingRadroach());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());

        triggerChapter();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.exiledCards).hasSize(2);

        saga.setCounterCount(CounterType.LORE, 2);
        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getId())
                .containsExactly(ownCreature.getCard().getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getId())
                .containsExactly(opponentCreature.getCard().getId());
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Vault 13: Dweller's Journey");
    }

    @Test
    void chapterIIIReturnsAuraAttachedToAChosenExistingCreature() {
        Permanent saga = addSagaWithLore(2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InfestingRadroach());
        AlmostPerfect aura = new AlmostPerfect();
        gd.addToExile(player2.getId(), aura, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).contains(creature.getId());
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        assertThat(findCardOnBattlefield(player2, aura.getId()).getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void chapterIIIKeepsAuraExiledWhenThereIsNothingItCanEnchant() {
        Permanent saga = addSagaWithLore(2);
        AlmostPerfect aura = new AlmostPerfect();
        gd.addToExile(player2.getId(), aura, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(aura.getId());
        harness.assertNotInGraveyard(player2, "Almost Perfect");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault13DwellersJourney());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findCardOnBattlefield(com.github.laxika.magicalvibes.model.Player player,
                                            java.util.UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
