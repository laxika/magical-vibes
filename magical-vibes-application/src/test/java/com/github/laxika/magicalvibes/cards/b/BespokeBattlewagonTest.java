package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BespokeBattlewagon.class, Forest.class, GrizzlyBears.class})
class BespokeBattlewagonTest extends BaseCardTest {

    @Test
    void tapsForTwoEnergy() {
        Permanent wagon = addReadyWagon();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    void paysEnergyToTapTargetCreature() {
        addReadyWagon();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void paysEnergyToDrawACard() {
        addReadyWagon();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void paysEnergyToBecomeAnArtifactCreatureUntilEndOfTurn() {
        Permanent wagon = addReadyWagon();
        gd.playerEnergyCounters.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.isArtifact(gd, wagon)).isTrue();
        assertThat(gqs.isCreature(gd, wagon)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isArtifact(gd, wagon)).isTrue();
        assertThat(gqs.isCreature(gd, wagon)).isFalse();
    }

    @Test
    void cannotPayEnergyWithoutEnoughCounters() {
        addReadyWagon();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }

    @Test
    void canTargetOnlyCreatures() {
        addReadyWagon();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyWagon() {
        return addReady(player1, new BespokeBattlewagon());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
