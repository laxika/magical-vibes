package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MineWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mine Worker gains 1 life without the other Workers")
    void gainsOneLifeWithoutWorkerAssembly() {
        Permanent mineWorker = addReadyMineWorker();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(mineWorker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gains 3 life when the controller has both named Workers")
    void gainsThreeLifeWithWorkerAssembly() {
        Permanent mineWorker = addReadyMineWorker();
        addNamedCreature(player1, "Power Plant Worker");
        addNamedCreature(player1, "Tower Worker");
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(mineWorker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent-controlled Workers do not enable the bonus")
    void opponentWorkersDoNotEnableBonus() {
        Permanent mineWorker = addReadyMineWorker();
        addNamedCreature(player2, "Power Plant Worker");
        addNamedCreature(player2, "Tower Worker");
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(mineWorker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Noncreatures with the Worker names do not enable the bonus")
    void noncreaturesDoNotEnableBonus() {
        Permanent mineWorker = addReadyMineWorker();
        addNamedPermanent(player1, "Power Plant Worker", CardType.ARTIFACT);
        addNamedCreature(player1, "Tower Worker");
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(mineWorker.isTapped()).isTrue();
    }

    private Permanent addReadyMineWorker() {
        Permanent mineWorker = harness.addToBattlefieldAndReturn(player1, new MineWorker());
        mineWorker.setSummoningSick(false);
        return mineWorker;
    }

    private Permanent addNamedCreature(Player player, String name) {
        return addNamedPermanent(player, name, CardType.CREATURE);
    }

    private Permanent addNamedPermanent(Player player, String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setPower(1);
        card.setToughness(1);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
