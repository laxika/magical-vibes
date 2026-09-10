package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TitaniaRuggedRumbler.class, Forest.class, Shock.class})
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
        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, discardHandCardIndex);
    }

    private Permanent addReadyTitania() {
        Permanent permanent = new Permanent(new TitaniaRuggedRumbler());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
