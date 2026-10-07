package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheaterOfHorrors.class, SauroformHybrid.class, Forest.class, DovinGrandArbiter.class})
class TheaterOfHorrorsTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, exiles the top card and tracks it with the enchantment")
    void upkeepExilesTopCardWithTheater() {
        Permanent theater = addTheater(player1);
        Card topCard = new SauroformHybrid();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(theater.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Exiled cards are not playable until an opponent loses life during your turn")
    void exiledCardsRequireOpponentLifeLoss() {
        Permanent theater = addTheater(player1);
        Card exiledCard = new SauroformHybrid();
        gd.addToExile(player1.getId(), exiledCard, theater.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, exiledCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The damage ability cannot target its controller")
    void damageAbilityCannotTargetController() {
        addTheater(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void emptyLibraryDoesNotCauseLifeLossOrAnExiledCard() {
        Permanent theater = addTheater(player1);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(theater.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void opponentsUpkeepDoesNotExileControllersLibrary() {
        Permanent theater = addTheater(player1);
        Card topCard = new SauroformHybrid();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.getCardsExiledByPermanent(theater.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void damageCannotTargetCreature() {
        addTheater(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void regainedLifeDoesNotRevokePermission() {
        Permanent theater = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertLife(player2, 20);
    }

    @Test
    void exiledLandUsesNormalLandPlayLimit() {
        Permanent theater = addTheater(player1);
        Card first = new Forest();
        Card second = new Forest();
        gd.addToExile(player1.getId(), first, theater.getId());
        gd.addToExile(player1.getId(), second, theater.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.castFromExile(player1, first.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(first).contains(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentLifeLossDoesNotPermitCastingDuringOpponentsTurn() {
        Permanent theater = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void controllersLifeLossDoesNotEnablePermission() {
        Permanent theater = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        harness.getLifeSupport().applyLifePayment(gd, player1.getId(), 1, "Life payment");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void creatureStillRequiresNormalCastingTiming() {
        Permanent theater = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void olderExiledCardsRemainPlayableAndAreNotCastForFree() {
        Permanent theater = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        gd.turnNumber++;
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.getCardsExiledByPermanent(theater.getId())).doesNotContain(card);
    }

    @Test
    void anotherTheaterDoesNotGrantPermissionForRemovedSourcesCards() {
        Permanent original = addTheater(player1);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        addTheater(player1);
        harness.getLifeSupport().applyLifePayment(gd, player2.getId(), 1, "Life payment");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    @Test
    void damageCanTargetOwnPlaneswalkerWithoutEnablingExilePermission() {
        Permanent theater = addTheater(player1);
        Permanent dovin = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());
        int loyaltyBefore = dovin.getCounterCount(CounterType.LOYALTY);
        Card card = new SauroformHybrid();
        gd.addToExile(player1.getId(), card, theater.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, dovin.getId());
        harness.passBothPriorities();

        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore - 1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No permission");
    }

    private Permanent addTheater(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TheaterOfHorrors());
    }
}
