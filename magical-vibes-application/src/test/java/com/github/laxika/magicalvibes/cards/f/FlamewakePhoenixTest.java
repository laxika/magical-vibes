package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlamewakePhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new FlamewakePhoenix());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Triggers at the beginning of combat when you control a creature with power 4 or greater")
    void triggersWithPowerFourCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{R}");
    }

    @Test
    @DisplayName("Does not trigger without a creature with power 4 or greater")
    void doesNotTriggerWithoutPowerFourCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(new FlamewakePhoenix()));

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Paying {R} returns Flamewake Phoenix from the graveyard to the battlefield")
    void payingRedReturnsPhoenix() {
        FlamewakePhoenix phoenix = new FlamewakePhoenix();
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(phoenix.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(phoenix.getId()));
    }

    @Test
    @DisplayName("Declining to pay keeps Flamewake Phoenix in the graveyard")
    void decliningKeepsPhoenixInGraveyard() {
        FlamewakePhoenix phoenix = new FlamewakePhoenix();
        harness.addToBattlefield(player1, new AirElemental());
        harness.setGraveyard(player1, List.of(phoenix));

        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(phoenix.getId()));
        harness.assertNotOnBattlefield(player1, "Flamewake Phoenix");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
