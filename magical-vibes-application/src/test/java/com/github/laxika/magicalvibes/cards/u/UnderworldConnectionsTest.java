package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnderworldConnections()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enchant a land an opponent controls")
    void canEnchantOpponentLand() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent opponentSwamp = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new UnderworldConnections()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, opponentSwamp.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> assertThat(p.getAttachedTo()).isEqualTo(opponentSwamp.getId()));
    }

    private Permanent attach(Player player) {
        harness.addToBattlefield(player, new Swamp());
        Permanent land = gd.playerBattlefields.get(player.getId()).getFirst();
        Permanent aura = new Permanent(new UnderworldConnections());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
        return land;
    }
}
