package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpearSpewer.class, GrotesqueDemise.class})
class SpearSpewerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each player")
    void deals1DamageToEachPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spearSpewer = addCreatureReady(player1, new SpearSpewer());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spearSpewer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SpearSpewer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent spearSpewer = addCreatureReady(player1, new SpearSpewer());
        spearSpewer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Damage waits for resolution and still resolves after the source is exiled")
    void resolvesAfterSourceIsExiled() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent spearSpewer = addCreatureReady(player1, new SpearSpewer());
        harness.setHand(player2, List.of(new GrotesqueDemise()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(spearSpewer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.castInstant(player2, 0, spearSpewer.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spear Spewer");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The opponent can activate their Spear Spewer and damage both players")
    void opponentActivationDamagesBothPlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new SpearSpewer());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
