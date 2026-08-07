package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecratogTest extends BaseCardTest {

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiling the top creature card of your graveyard gives Necratog +2/+2")
    void exilingTopCreatureCardBoosts() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Permanent necratog = findPermanent(player1, "Necratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(necratog.getPowerModifier()).isEqualTo(2);
        assertThat(necratog.getToughnessModifier()).isEqualTo(2);
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped, not blockers")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GiantGrowth()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).containsExactly("Giant Growth");
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        Permanent necratog = findPermanent(player1, "Necratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(necratog.getPowerModifier()).isEqualTo(0);
        assertThat(necratog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card in graveyard to exile");
    }
}
