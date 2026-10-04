package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BaronHelmutZemo.class, DarkRitual.class, GrizzlyBears.class, Swamp.class, BoneSplinters.class})
class BaronHelmutZemoTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void clearStartingHand() {
        harness.setHand(player1, List.of());
    }

    @Test
    void blackSpellCastFromHandCausesConnive() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        harness.setHand(player1, List.of(new DarkRitual(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void boastCopiesTheBlackSpellsPaidForThatActivationAndCastsUpToThree() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        List<Card> rituals = IntStream.range(0, 15)
                .mapToObj(ignored -> (Card) new DarkRitual())
                .toList();
        Card nonBlackCard = new GrizzlyBears();
        List<Card> graveyard = new ArrayList<>(rituals);
        graveyard.add(nonBlackCard);
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice costChoice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        assertThat(costChoice.cards()).containsExactlyElementsOf(rituals);
        harness.handleMultipleCardsChosen(player1, rituals.stream().map(Card::getId).toList());
        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).containsExactlyElementsOf(rituals);

        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).hasSize(15);
        assertThat(castChoice.maxCount()).isEqualTo(3);
        List<UUID> chosenCopies = castChoice.validCardIds().subList(0, 3);
        harness.handleMultipleCardsChosen(player1, chosenCopies);
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).containsExactlyElementsOf(rituals);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonBlackCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(9);
    }

    @Test
    void discardingALandDoesNotAddACounter() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void nonBlackSpellFromHandDoesNotCauseConnive() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentsBlackSpellDoesNotCauseConnive() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void boastRequiresAttackingThisTurn() {
        addCreatureReady(player1, new BaronHelmutZemo());
        harness.setGraveyard(player1, IntStream.range(0, 15)
                .mapToObj(ignored -> (Card) new DarkRitual()).toList());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(15);
    }

    @Test
    void boastCannotBePaidWithFewerThanFifteenBlackSymbols() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        harness.setGraveyard(player1, IntStream.range(0, 14)
                .mapToObj(ignored -> (Card) new DarkRitual()).toList());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(14);
        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).isEmpty();
    }

    @Test
    void mayDeclineAllCopiesButCannotBoastAgainThisTurn() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        List<Card> rituals = IntStream.range(0, 30)
                .mapToObj(ignored -> (Card) new DarkRitual()).toList();
        harness.setGraveyard(player1, rituals);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, rituals.subList(0, 15).stream().map(Card::getId).toList());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).hasSize(15);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void boastCountsEachBlackSymbolAndCopiesDoNotCauseConnive() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        List<Card> graveyard = new ArrayList<>(IntStream.range(0, 5)
                .mapToObj(ignored -> (Card) new BaronHelmutZemo()).toList());
        graveyard.add(new DarkRitual());
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, graveyard.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        UUID ritualCopy = choice.validCardIds().stream()
                .filter(id -> gd.findExiledCard(id).card().getName().equals("Dark Ritual"))
                .findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(ritualCopy));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).containsExactlyElementsOf(graveyard);
    }

    @Test
    void copiedSpellCanRequireAnAdditionalSacrificeCost() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Card splinters = new BoneSplinters();
        List<Card> graveyard = new ArrayList<>(IntStream.range(0, 14)
                .mapToObj(ignored -> (Card) new DarkRitual()).toList());
        graveyard.add(splinters);
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, graveyard.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        PendingInteraction.ImprovisationCapstoneCastChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        UUID splintersCopy = choice.validCardIds().stream()
                .filter(id -> gd.findExiledCard(id).card().getName().equals("Bone Splinters"))
                .findFirst().orElseThrow();

        harness.handleMultipleCardsChosen(player1, List.of(splintersCopy));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).contains(splinters);
    }
}
