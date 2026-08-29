package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
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

@CardUsed({AngelsGrace.class, AcolyteOfXathrid.class, Shock.class})
class AngelsGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Damage reduces the controller's life to 1 without ending the game")
    void damageReducesLifeToOne() {
        harness.setLife(player1, 2);
        castAngelsGrace();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage floor still applies after life loss leaves the controller at 0")
    void damageFloorAppliesAfterLifeLoss() {
        harness.setLife(player1, 1);
        castAngelsGrace();

        Permanent acolyte = new Permanent(new AcolyteOfXathrid());
        acolyte.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(acolyte);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Split second blocks spells and non-mana activated abilities")
    void splitSecondBlocksResponses() {
        Permanent acolyte = new Permanent(new AcolyteOfXathrid());
        acolyte.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(acolyte);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new AngelsGrace()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Angel's Grace expires during cleanup")
    void expiresAtCleanup() {
        harness.setLife(player1, 1);
        castAngelsGrace();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(-1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private void castAngelsGrace() {
        harness.setHand(player1, List.of(new AngelsGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
