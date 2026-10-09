package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptGhast.class, GrizzlyBears.class, Swamp.class, Plains.class})
class CryptGhastTest extends BaseCardTest {

    @Test
    @DisplayName("Paying Extort drains the opponent and gains life")
    void payingExtortDrainsOpponent() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Declining Extort does nothing")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping your Swamp adds an additional black mana")
    void ownSwampProducesExtraBlack() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's Swamp does not produce Crypt Ghast's additional mana")
    void opponentSwampDoesNotProduceExtraBlack() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player2, new Swamp());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void extortWaitsUntilResolutionBeforeOfferingPayment() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void blackManaCanPayExtort() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void multipleGhastsEachAddOneBlackManaWithoutUsingStack() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player1, new Swamp());

        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void nonSwampDoesNotProduceExtraMana() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player1, new Plains());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void castingGhastDoesNotTriggerItsOwnExtort() {
        harness.castFromHand(player1, new CryptGhast(), "{3}{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crypt Ghast");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentCastingSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleGhastsOfferSeparateExtortPayments() {
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addToBattlefield(player1, new CryptGhast());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
