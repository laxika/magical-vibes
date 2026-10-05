package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GolgariGuildgate;
import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilferingImp.class, BartizanBats.class, GolgariGuildgate.class})
class PilferingImpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Pilfering Imp and targets an opponent")
    void activatingSacrificesSelfAndTargetsOpponent() {
        addReadyPilferingImp(player1);
        readyForSorcerySpeed();
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Pilfering Imp");
        harness.assertNotOnBattlefield(player1, "Pilfering Imp");
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
                    assertThat(entry.getTargetId()).isEqualTo(player2.getId());
                });
    }

    @Test
    @DisplayName("Reveals the opponent's hand and only allows choosing a nonland card")
    void choosesNonlandCardToDiscard() {
        addReadyPilferingImp(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new BartizanBats(), new GolgariGuildgate())));
        readyForSorcerySpeed();
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Bartizan Bats");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Golgari Guildgate");
    }

    @Test
    @DisplayName("Cannot activate at instant speed")
    void cannotActivateAtInstantSpeed() {
        addReadyPilferingImp(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReadyPilferingImp(player1);
        readyForSorcerySpeed();
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty hand finishes resolving without a discard choice")
    void emptyHandDoesNotRequireChoice() {
        addReadyPilferingImp(player1);
        harness.setHand(player2, List.of());
        readyForSorcerySpeed();
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Pilfering Imp");
    }

    @Test
    @DisplayName("A hand containing only lands stays intact")
    void landOnlyHandDoesNotRequireChoice() {
        addReadyPilferingImp(player1);
        GolgariGuildgate land = new GolgariGuildgate();
        harness.setHand(player2, List.of(land));
        readyForSorcerySpeed();
        addMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        harness.assertNotInGraveyard(player2, "Golgari Guildgate");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent imp = addReadyPilferingImp(player1);
        imp.setSummoningSick(true);
        readyForSorcerySpeed();
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pilfering Imp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Imp cannot activate its ability")
    void cannotActivateWhileTapped() {
        addReadyPilferingImp(player1).setTapped(true);
        readyForSorcerySpeed();
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pilfering Imp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires both the generic and black mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyPilferingImp(player1);
        readyForSorcerySpeed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pilfering Imp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation is forbidden outside a main phase")
    void cannotActivateDuringCombat() {
        addReadyPilferingImp(player1);
        readyForSorcerySpeed();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Pilfering Imp");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPilferingImp(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new PilferingImp());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void readyForSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
