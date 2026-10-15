package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandrasRegulator.class, ChandraNovicePyromancer.class, AirElemental.class,
        GrizzlyBears.class, Mountain.class, Shock.class})
class ChandrasRegulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} to copy a Chandra loyalty ability")
    void copiesChandraLoyaltyAbility() {
        harness.addToBattlefield(player1, new ChandrasRegulator());
        Permanent chandra = addReadyChandra(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Discards a Mountain card to draw a card")
    void discardsMountainAndDraws() {
        Permanent regulator = harness.addToBattlefieldAndReturn(player1, new ChandrasRegulator());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(regulator.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Discards a red card to draw a card")
    void discardsRedCardAndDraws() {
        harness.addToBattlefield(player1, new ChandrasRegulator());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void decliningPaymentLeavesLoyaltyAbilityUncopied() {
        harness.addToBattlefield(player1, new ChandrasRegulator());
        Permanent chandra = addReadyChandra(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesManaProducingLoyaltyAbilityWithoutPayingLoyaltyAgain() {
        harness.addToBattlefield(player1, new ChandrasRegulator());
        Permanent chandra = addReadyChandra(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyCanChooseNewTargetWhileOriginalKeepsItsTarget() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ChandrasRegulator());
        Permanent chandra = addReadyChandra(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsChandraDoesNotTriggerRegulator() {
        harness.addToBattlefield(player1, new ChandrasRegulator());
        addReadyChandra(player2);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsNonRedNonMountainDiscardWithoutDrawing() {
        Permanent regulator = harness.addToBattlefieldAndReturn(player1, new ChandrasRegulator());
        harness.setHand(player1, List.of(new AirElemental(), new Mountain()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(regulator.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyChandra(Player player) {
        Permanent chandra = harness.addToBattlefieldAndReturn(player, new ChandraNovicePyromancer());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        chandra.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }

}
