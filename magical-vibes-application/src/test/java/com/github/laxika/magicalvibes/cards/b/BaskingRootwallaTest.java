package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.u.Unhinge;
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

@CardUsed({BaskingRootwalla.class, Unhinge.class})
class BaskingRootwallaTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Basking Rootwalla +2/+2 until end of turn")
    void resolvingAbilityBoostsSelf() {
        Permanent rootwalla = addCreatureReady(player1, new BaskingRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump ability can be activated only once each turn")
    void pumpAbilityOnlyOncePerTurn() {
        addCreatureReady(player1, new BaskingRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The pump ability can be activated again on a new turn")
    void pumpAbilityResetsEachTurn() {
        Permanent rootwalla = addCreatureReady(player1, new BaskingRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump wears off at end of turn cleanup")
    void pumpWearsOffAtEndOfTurn() {
        Permanent rootwalla = addCreatureReady(player1, new BaskingRootwalla());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(rootwalla.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rootwalla.getEffectivePower()).isEqualTo(1);
        assertThat(rootwalla.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding Basking Rootwalla offers its zero-cost madness cast")
    void discardTriggersMadness() {
        BaskingRootwalla rootwalla = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rootwalla.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness puts Basking Rootwalla into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        BaskingRootwalla rootwalla = discardViaUnhinge();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(rootwalla.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(rootwalla.getId()));
    }

    @Test
    @DisplayName("Accepting zero-cost madness puts Basking Rootwalla onto the battlefield")
    void acceptingMadnessCastsForNoMana() {
        BaskingRootwalla rootwalla = discardViaUnhinge();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(rootwalla.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private BaskingRootwalla discardViaUnhinge() {
        BaskingRootwalla rootwalla = new BaskingRootwalla();
        harness.setHand(player1, List.of(rootwalla));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return rootwalla;
    }
}
