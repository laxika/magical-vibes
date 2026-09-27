package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhituFire.class, ArdentSoldier.class})
class GhituFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to a player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X damage to a creature")
    void dealsXDamageToCreature() {
        Permanent target = addCreatureReady(player2, new ArdentSoldier());
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertInGraveyard(player2, "Ardent Soldier");
    }

    @Test
    @DisplayName("Can be cast on an opponent's turn by paying {2} more")
    void flashCastForTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghitu Fire");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flash cast uses the chosen X value")
    void flashCastUsesChosenXValue() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.ensurePriority(player1);
        gs.playCardWithAlternateCost(gd, player1, 0, 2, player2.getId(), null, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be hardcast on an opponent's turn")
    void noFlashWithoutTheSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
