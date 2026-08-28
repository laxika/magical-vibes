package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderworldBreach.class, Shock.class})
class UnderworldBreachTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a nonland card from the graveyard by exiling three other cards")
    void castsSpellWithEscape() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(0, 1, 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Cannot cast a spell with escape without three other graveyard cards")
    void requiresThreeOtherGraveyardCards() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("escape");
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of its controller's next end step")
    void sacrificesAtNextEndStep() {
        harness.addToBattlefield(player1, new UnderworldBreach());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Underworld Breach");
        harness.assertInGraveyard(player1, "Underworld Breach");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
