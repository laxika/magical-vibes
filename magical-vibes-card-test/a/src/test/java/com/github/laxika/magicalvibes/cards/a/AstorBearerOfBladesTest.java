package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldenArgosy;
import com.github.laxika.magicalvibes.cards.h.HerosHeirloom;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StrixhavenSkycoach;
import com.github.laxika.magicalvibes.cards.s.SalvagedManaworker;
import com.github.laxika.magicalvibes.cards.w.WarlordsAxe;
import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AstorBearerOfBlades.class, GrizzlyBears.class, Plains.class,
        StrixhavenSkycoach.class, WarlordsAxe.class, GoldenArgosy.class, HerosHeirloom.class,
        YavimayaIconoclast.class, SalvagedManaworker.class})
class AstorBearerOfBladesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers one Equipment or Vehicle from the top seven")
    void etbOffersEquipmentOrVehicle() {
        Card equipment = new WarlordsAxe();
        Card vehicle = new StrixhavenSkycoach();
        List<Card> topCards = List.of(
                new GrizzlyBears(), equipment, new Plains(), vehicle,
                new GrizzlyBears(), new Plains(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);
        castAstorAndResolveTrigger();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(7);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(equipment.getId(), vehicle.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.stream()
                        .filter(card -> card != equipment)
                        .toList());
    }

    @Test
    @DisplayName("Equipment you control gains an equip {1} ability")
    void equipmentGainsEquipOneAbility() {
        addReady(player1, new AstorBearerOfBlades());
        Permanent equipment = addReady(player1, new WarlordsAxe());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Vehicles you control gain a crew 1 ability")
    void vehicleGainsCrewOneAbility() {
        addReady(player1, new AstorBearerOfBlades());
        Permanent vehicle = addReady(player1, new StrixhavenSkycoach());
        Permanent creature = addReady(player1, new GrizzlyBears());

        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, 1, 1, null, null);
            harness.handlePermanentChosen(player1, creature.getId());
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();
        });

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void mayDeclineTheOnlyEligibleCardInAShortLibrary() {
        Card equipment = new HerosHeirloom();
        List<Card> library = List.of(new Plains(), equipment, new Plains());
        harness.setLibrary(player1, library);

        castAstorAndResolveTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void mayDeclineWhenSeveralEligibleCardsAreFound() {
        List<Card> library = List.of(new HerosHeirloom(), new GoldenArgosy(), new Plains());
        harness.setLibrary(player1, library);

        castAstorAndResolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void mayChooseVehicleAndLeavesUnlookedCardsAboveTheRest() {
        Card vehicle = new GoldenArgosy();
        Card eighthCard = new Plains();
        List<Card> topSeven = List.of(new HerosHeirloom(), vehicle, new Plains(), new Plains(),
                new Plains(), new Plains(), new Plains());
        harness.setLibrary(player1, List.of(topSeven.get(0), topSeven.get(1), topSeven.get(2),
                topSeven.get(3), topSeven.get(4), topSeven.get(5), topSeven.get(6), eighthCard));

        castAstorAndResolveTrigger();
        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(vehicle);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSeven.stream().filter(card -> card != vehicle).toList());
    }

    @Test
    void noEligibleCardsReturnsAllLookedCardsToTheLibrary() {
        List<Card> library = List.of(new Plains(), new Plains(), new Plains());
        harness.setLibrary(player1, library);

        castAstorAndResolveTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventAstorFromEntering() {
        harness.setLibrary(player1, List.of());

        castAstorAndResolveTrigger();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    void originalEquipAbilityRemainsAvailableAfterAstorLeaves() {
        Permanent astor = addReady(player1, new AstorBearerOfBlades());
        Permanent equipment = addReady(player1, new HerosHeirloom());
        Permanent target = addReady(player1, new YavimayaIconoclast());
        gd.playerBattlefields.get(player1.getId()).remove(astor);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentEquipmentDoesNotGainEquipOne() {
        addReady(player2, new AstorBearerOfBlades());
        Permanent equipment = addReady(player1, new HerosHeirloom());
        Permanent creature = addReady(player1, new YavimayaIconoclast());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void grantedCrewAbilityAcceptsOnePowerAndSummoningSickCreatures() {
        addReady(player1, new AstorBearerOfBlades());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new GoldenArgosy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SalvagedManaworker());
        creature.setSummoningSick(true);

        harness.withAutoStop(gd.currentStep, () -> {
            harness.activateAbility(player1, 1, 1, null, null);
            harness.handlePermanentChosen(player1, creature.getId());
            harness.handlePermanentChosen(player1, player1.getId());
            harness.passBothPriorities();
        });

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
    }

    @Test
    void equipmentLosesTheCheaperEquipCostWhenAstorLeaves() {
        Permanent astor = addReady(player1, new AstorBearerOfBlades());
        Permanent equipment = addReady(player1, new HerosHeirloom());
        Permanent creature = addReady(player1, new YavimayaIconoclast());
        gd.playerBattlefields.get(player1.getId()).remove(astor);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private void castAstorAndResolveTrigger() {
        harness.setHand(player1, List.of(new AstorBearerOfBlades()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
