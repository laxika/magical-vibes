package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PowerPlantWorker;
import com.github.laxika.magicalvibes.cards.t.TowerWorker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MineWorker.class, PowerPlantWorker.class, TowerWorker.class})
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
        return addCreatureReady(player1, new MineWorker());
    }

    @Test
    void gainsOnlyOneLifeWithOnlyPowerPlantWorker() {
        addReadyMineWorker();
        harness.addToBattlefield(player1, new PowerPlantWorker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void gainsOnlyOneLifeWithOnlyTowerWorker() {
        addReadyMineWorker();
        harness.addToBattlefield(player1, new TowerWorker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void checksWorkerAssemblyAtResolutionRatherThanActivation() {
        addReadyMineWorker();
        harness.addToBattlefield(player1, new PowerPlantWorker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new TowerWorker());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void losesBonusWhenTowerWorkerLeavesBeforeResolution() {
        addReadyMineWorker();
        harness.addToBattlefield(player1, new PowerPlantWorker());
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerWorker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(tower);
        gd.playerGraveyards.get(player1.getId()).add(tower.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void abilityResolvesAfterMineWorkerLeavesBattlefield() {
        Permanent mine = addReadyMineWorker();
        harness.addToBattlefield(player1, new PowerPlantWorker());
        harness.addToBattlefield(player1, new TowerWorker());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(mine);
        gd.playerGraveyards.get(player1.getId()).add(mine.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new MineWorker());
        mine.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mine.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
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
