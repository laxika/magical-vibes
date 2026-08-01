package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NivmagusElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a controlled instant from the stack and puts two counters on Nivmagus Elemental")
    void exilesControlledInstantAndAddsCounters() {
        Permanent nivmagus = addCreatureReady(player1, new NivmagusElemental());
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileInstantOrSorcerySpellCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).containsExactly(shock.getId());

        harness.passBothPriorities();

        assertThat(nivmagus.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot exile an instant controlled by another player")
    void cannotExileOpponentSpell() {
        harness.addToBattlefield(player1, new NivmagusElemental());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(shock);
    }
}
