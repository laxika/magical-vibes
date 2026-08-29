package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        addNamedCreature(player1, "Mine Worker");
        addNamedCreature(player1, "Power Plant Worker");

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent-controlled Workers do not enable the bonus")
    void opponentWorkersDoNotEnableBonus() {
        addReadyTowerWorker();
        addNamedCreature(player2, "Mine Worker");
        addNamedCreature(player2, "Power Plant Worker");

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

    private Permanent addReadyTowerWorker() {
        Permanent towerWorker = harness.addToBattlefieldAndReturn(player1, new TowerWorker());
        towerWorker.setSummoningSick(false);
        return towerWorker;
    }

    private void addNamedCreature(Player player, String name) {
        addNamedPermanent(player, name, CardType.CREATURE);
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
