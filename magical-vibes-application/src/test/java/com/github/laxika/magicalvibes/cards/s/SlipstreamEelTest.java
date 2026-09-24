package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipstreamEel.class, Island.class, Mountain.class})
class SlipstreamEelTest extends BaseCardTest {

    @Test
    @DisplayName("Slipstream Eel can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new SlipstreamEel());
        declareAttackers(List.of(0));

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Slipstream Eel cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderControlsNoIsland() {
        addCreatureReady(player1, new SlipstreamEel());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slipstream Eel cannot attack when defending player controls a Mountain")
    void cannotAttackWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        addCreatureReady(player1, new SlipstreamEel());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Slipstream Eel cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackerControlsIsland() {
        addCreatureReady(player1, new SlipstreamEel());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling Slipstream Eel discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new SlipstreamEel()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Slipstream Eel");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Cycling Slipstream Eel requires blue mana")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new SlipstreamEel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
