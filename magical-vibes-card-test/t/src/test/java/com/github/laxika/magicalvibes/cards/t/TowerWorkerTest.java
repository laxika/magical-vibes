package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MineWorker;
import com.github.laxika.magicalvibes.cards.p.PowerPlantWorker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerWorker.class, MineWorker.class, PowerPlantWorker.class})
class TowerWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Tower Worker adds one colorless mana without the other Workers")
    void addsOneManaWithoutWorkerAssembly() {
        Permanent towerWorker = addReadyTowerWorker();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(towerWorker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Tower Worker adds three colorless mana with both named Workers")
    void addsThreeManaWithWorkerAssembly() {
        addReadyTowerWorker();
        harness.addToBattlefield(player1, new MineWorker());
        harness.addToBattlefield(player1, new PowerPlantWorker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Workers do not enable the bonus")
    void opponentWorkersDoNotEnableBonus() {
        addReadyTowerWorker();
        harness.addToBattlefield(player2, new MineWorker());
        harness.addToBattlefield(player2, new PowerPlantWorker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreatures with the Worker names do not enable the bonus")
    void noncreaturesDoNotEnableBonus() {
        addReadyTowerWorker();
        addNamedPermanent(player1, "Mine Worker", CardType.ARTIFACT);
        addNamedPermanent(player1, "Power Plant Worker", CardType.ARTIFACT);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void mineWorkerAloneDoesNotEnableBonus() {
        addReadyTowerWorker();
        harness.addToBattlefield(player1, new MineWorker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void powerPlantWorkerAloneDoesNotEnableBonus() {
        addReadyTowerWorker();
        harness.addToBattlefield(player1, new PowerPlantWorker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void workersSplitBetweenPlayersDoNotEnableBonus() {
        addReadyTowerWorker();
        harness.addToBattlefield(player1, new MineWorker());
        harness.addToBattlefield(player2, new PowerPlantWorker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tappedWorkersStillEnableBonusAndManaDoesNotUseStack() {
        addReadyTowerWorker();
        harness.addToBattlefieldAndReturn(player1, new MineWorker()).tap();
        harness.addToBattlefieldAndReturn(player1, new PowerPlantWorker()).tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickTowerWorkerCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new TowerWorker()).setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void tappedTowerWorkerCannotActivateAgain() {
        addReadyTowerWorker();
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyTowerWorker() {
        Permanent towerWorker = harness.addToBattlefieldAndReturn(player1, new TowerWorker());
        towerWorker.setSummoningSick(false);
        return towerWorker;
    }

    private void addNamedPermanent(Player player, String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setPower(1);
        card.setToughness(1);
        harness.addToBattlefieldAndReturn(player, card);
    }
}
