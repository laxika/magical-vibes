package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
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

@CardUsed({Necratog.class, BenalishInfantry.class, StripedBears.class, Vitalize.class})
class NecratogTest extends BaseCardTest {

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(c -> c.getName()).toList();
    }

    @Test
    @DisplayName("Exiling the top creature card of your graveyard gives Necratog +2/+2")
    void exilingTopCreatureCardBoosts() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        Permanent necratog = findPermanent(player1, "Necratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(necratog.getPowerModifier()).isEqualTo(2);
        assertThat(necratog.getToughnessModifier()).isEqualTo(2);
        assertThat(graveyardNames(player1)).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).contains("Benalish Infantry");
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped, not blockers")
    void skipsNoncreatureCardsAboveIt() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new BenalishInfantry(), new Vitalize()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).containsExactly("Vitalize");
    }

    @Test
    @DisplayName("The nearest creature card is exiled when multiple creature cards are present")
    void exilesNearestCreatureCard() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(
                new BenalishInfantry(), new StripedBears(), new Vitalize()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(graveyardNames(player1)).containsExactly("Benalish Infantry", "Vitalize");
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Striped Bears");
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new BenalishInfantry()));

        Permanent necratog = findPermanent(player1, "Necratog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(necratog.getPowerModifier()).isEqualTo(0);
        assertThat(necratog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player1, List.of(new Vitalize()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card in graveyard to exile");
    }

    @Test
    @DisplayName("Cannot use an opponent's creature card to pay the ability's cost")
    void cannotUseOpponentsGraveyard() {
        harness.addToBattlefield(player1, new Necratog());
        harness.setGraveyard(player2, List.of(new BenalishInfantry()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card in graveyard to exile");

        assertThat(graveyardNames(player2)).containsExactly("Benalish Infantry");
        assertThat(gd.exiledCards).isEmpty();
    }
}
