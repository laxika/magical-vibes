package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({UnderworldConnections.class, Swamp.class, DrudgeBeetle.class})
class UnderworldConnectionsTest extends BaseCardTest {

    @Test
    @DisplayName("Granted ability taps the land, pays 1 life and draws a card")
    void grantedAbilityDrawsForOneLife() {
        Permanent swamp = attach(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(swamp.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Granted ability cannot be activated while the land is tapped")
    void cannotActivateWhileTapped() {
        Permanent swamp = attach(player1);
        swamp.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a creature")
    void cannotEnchantCreature() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new UnderworldConnections()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, beetle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant a land an opponent controls")
    void canEnchantOpponentLand() {
        Permanent opponentSwamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new UnderworldConnections()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, opponentSwamp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> assertThat(p.getAttachedTo()).isEqualTo(opponentSwamp.getId()));
    }

    private Permanent attach(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new Swamp());
        Permanent aura = harness.addToBattlefieldAndReturn(player, new UnderworldConnections());
        aura.setAttachedTo(land.getId());
        return land;
    }

    @Test
    @DisplayName("The enchanted land's controller pays life and draws, even with an opposing Aura")
    void opponentControlsGrantedAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnderworldConnections());
        aura.setAttachedTo(land.getId());
        harness.setLibrary(player2, List.of(new Swamp()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int life1 = gd.playerLifeTotals.get(player1.getId());
        int life2 = gd.playerLifeTotals.get(player2.getId());
        int hand1 = gd.playerHands.get(player1.getId()).size();
        int hand2 = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        harness.assertLife(player1, life1);
        harness.assertLife(player2, life2 - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(hand1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(hand2 + 1);
    }

    @Test
    @DisplayName("Tap and life are paid immediately, and removing the Aura does not stop the pending draw")
    void costsArePaidBeforeResolutionAndAbilitySurvivesAuraRemoval() {
        Permanent land = attach(player1);
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof UnderworldConnections);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertLife(player1, lifeBefore - 1);
        land.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
