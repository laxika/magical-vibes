package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.StrixhavenSkycoach;
import com.github.laxika.magicalvibes.cards.w.WarlordsAxe;
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

@CardUsed({AstorBearerOfBlades.class, GrizzlyBears.class, Plains.class,
        StrixhavenSkycoach.class, WarlordsAxe.class})
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
        harness.setHand(player1, List.of(new AstorBearerOfBlades()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
