package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeDespair.class, GiantSpider.class, GrizzlyBears.class, LilianaVess.class,
        PhyrexianArena.class})
class InvokeDespairTest extends BaseCardTest {

    @Test
    @DisplayName("The target opponent sacrifices a creature, enchantment, and planeswalker")
    void sacrificesEachPermanentType() {
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castInvokeDespair(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, chosenCreature.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Giant Spider", "Phyrexian Arena", "Liliana Vess");
    }

    @Test
    @DisplayName("The target opponent loses life and the controller draws when types are unavailable")
    void fallbackAppliesForUnavailableTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GiantSpider(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        castInvokeDespair(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Invoke Despair can target only an opponent")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new InvokeDespair()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castInvokeDespair(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new InvokeDespair()));
        addMana(player1);
        harness.castSorcery(player1, 0, targetPlayerId);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 4);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
