package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WrithingNecromass.class, WalkingBulwark.class, LightningStrike.class})
class WrithingNecromassTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with an empty graveyard")
    void canCastForFullCost() {
        harness.castFromHand(player1, new WrithingNecromass(), "{6}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for each creature card in its controller's graveyard")
    void costsLessForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new WalkingBulwark(), new WalkingBulwark(), new WalkingBulwark()));
        harness.castFromHand(player1, new WrithingNecromass(), "{3}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Non-creature cards do not reduce its cost")
    void nonCreatureCardsDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.setHand(player1, List.of(new WrithingNecromass()));
        addMana(5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent's graveyard creatures do not reduce its cost")
    void opponentGraveyardDoesNotReduceCost() {
        harness.setGraveyard(player2, List.of(new WalkingBulwark(), new WalkingBulwark()));
        harness.setHand(player1, List.of(new WrithingNecromass()));
        addMana(5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Excess creature cards reduce only the generic cost")
    void excessCreaturesLeaveBlackManaCost() {
        harness.setGraveyard(player1, List.of(new WalkingBulwark(), new WalkingBulwark(),
                new WalkingBulwark(), new WalkingBulwark(), new WalkingBulwark(),
                new WalkingBulwark(), new WalkingBulwark()));

        harness.castFromHand(player1, new WrithingNecromass(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Writhing Necromass");
    }

    @Test
    @DisplayName("Cost reduction cannot replace the required black mana")
    void reductionCannotPayBlackMana() {
        harness.setGraveyard(player1, List.of(new WalkingBulwark(), new WalkingBulwark(),
                new WalkingBulwark(), new WalkingBulwark(), new WalkingBulwark(),
                new WalkingBulwark(), new WalkingBulwark()));
        harness.setHand(player1, List.of(new WrithingNecromass()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Writhing Necromass");
    }

    @Test
    @DisplayName("Only creatures count in a mixed graveyard, including artifact creatures")
    void mixedGraveyardCountsOnlyCreatures() {
        harness.setGraveyard(player1, List.of(new WalkingBulwark(), new LightningStrike(),
                new WalkingBulwark(), new LightningStrike()));

        harness.castFromHand(player1, new WrithingNecromass(), "{4}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addMana(int colorless) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
    }
}
