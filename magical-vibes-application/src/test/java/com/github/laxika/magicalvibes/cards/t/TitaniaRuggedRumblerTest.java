package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BullseyeDeathDealer;
import com.github.laxika.magicalvibes.cards.c.CaptainAmericasShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TitaniaRuggedRumbler.class, Forest.class, Shock.class,
        BullseyeDeathDealer.class, CaptainAmericasShield.class})
class TitaniaRuggedRumblerTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card for the additional cost and enters the battlefield")
    void discardsForAdditionalCost() {
        harness.setHand(player1, List.of(new TitaniaRuggedRumbler(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        castTitania(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Titania, Rugged Rumbler");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Pays {2} for the additional cost and enters the battlefield")
    void paysForAdditionalCost() {
        harness.setHand(player1, List.of(new TitaniaRuggedRumbler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        castTitania(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Titania, Rugged Rumbler");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without discarding a card or paying the additional cost")
    void requiresAdditionalCostPayment() {
        harness.setHand(player1, List.of(new TitaniaRuggedRumbler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> castTitania(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Ward can be paid with mana when discarding is also available")
    void wardCanBePaidWithMana() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, titania.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(titania.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Ward can be paid by discarding when its mana cost cannot be paid")
    void wardCanBePaidByDiscarding() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, titania.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(titania.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Ward counters the spell when its controller pays neither option")
    void wardCountersWhenNeitherOptionIsPaid() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, titania.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(titania.getMarkedDamage()).isZero();
    }

    private void castTitania(Integer discardHandCardIndex) {
        if (discardHandCardIndex == null) {
            harness.castCreature(player1, 0);
        } else {
            harness.castSorceryWithDiscard(player1, 0, discardHandCardIndex);
        }
    }

    private Permanent addReadyTitania() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new TitaniaRuggedRumbler());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Ward allows discarding instead of paying when both options are available")
    void wardCanDiscardInsteadOfPayingMana() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new Forest()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, titania.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(titania.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an activated ability when both available payments are declined")
    void wardCountersActivatedAbilityWhenBothPaymentsAreDeclined() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        Permanent bullseye = harness.addToBattlefieldAndReturn(player2, new BullseyeDeathDealer());
        bullseye.setSummoningSick(false);
        harness.addToBattlefield(player2, new CaptainAmericasShield());
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, 0, null, titania.getId());
        harness.assertInGraveyard(player2, "Captain America's Shield");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(titania.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Forest");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent titania = addReadyTitania();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, titania.getId());

        assertThat(titania.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying ward for an activated ability actually spends the mana")
    void wardManaPaymentForActivatedAbilitySpendsMana() {
        Permanent titania = addReadyTitania();
        beginOpponentTurn();
        Permanent bullseye = harness.addToBattlefieldAndReturn(player2, new BullseyeDeathDealer());
        bullseye.setSummoningSick(false);
        harness.addToBattlefield(player2, new CaptainAmericasShield());
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.activateAbility(player2, 0, 0, null, titania.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        assertThat(titania.getMarkedDamage()).isEqualTo(2);
        harness.assertInHand(player2, "Forest");
    }
}
