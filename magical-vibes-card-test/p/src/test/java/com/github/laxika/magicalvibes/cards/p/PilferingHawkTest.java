package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FrostAugur;
import com.github.laxika.magicalvibes.cards.b.BeholdTheMultiverse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
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

@CardUsed({PilferingHawk.class, BeholdTheMultiverse.class, FrostAugur.class})
class PilferingHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and then discards a card")
    void drawsThenDiscards() {
        addReadyHawk();
        BeholdTheMultiverse drawnCard = new BeholdTheMultiverse();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new FrostAugur()));
        addSnowMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Behold the Multiverse");
        harness.assertInGraveyard(player1, "Frost Augur");
    }

    @Test
    @DisplayName("Requires snow mana to activate")
    void requiresSnowMana() {
        addReadyHawk();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Can discard the card just drawn with an initially empty hand")
    void discardsDrawnCardFromEmptyHand() {
        addReadyHawk();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BeholdTheMultiverse()));
        addSnowMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Behold the Multiverse");
    }

    @Test
    @DisplayName("Snow mana of a different color pays the cost and the Hawk taps immediately")
    void acceptsGreenSnowManaAndPaysTapCost() {
        addReadyHawk();
        harness.setHand(player1, List.of(new FrostAugur()));
        harness.setLibrary(player1, List.of(new BeholdTheMultiverse()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        harness.assertNotInHand(player1, "Behold the Multiverse");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Behold the Multiverse");
        harness.assertInGraveyard(player1, "Frost Augur");
    }

    @Test
    @DisplayName("A tapped Hawk cannot activate")
    void cannotActivateWhileTapped() {
        addReadyHawk();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        addSnowMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Hawk cannot activate")
    void cannotActivateWithSummoningSickness() {
        addReadyHawk();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);
        addSnowMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyHawk() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new PilferingHawk());
        hawk.setSummoningSick(false);
    }

    private void addSnowMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLUE, 1);
    }
}
