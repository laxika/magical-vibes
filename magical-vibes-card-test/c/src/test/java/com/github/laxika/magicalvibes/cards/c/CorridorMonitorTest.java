package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.i.IdyllicGrange;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorridorMonitor.class, GoldenEgg.class, IdyllicGrange.class, YouthfulKnight.class})
class CorridorMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and untaps a tapped creature you control")
    void untapsCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        creature.tap();
        castCorridorMonitor(creature);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters and untaps a tapped artifact you control")
    void untapsArtifactYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        artifact.tap();
        castCorridorMonitor(artifact);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature you control");
    }

    @Test
    void cannotTargetOrdinaryLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new IdyllicGrange());
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature you control");
    }

    @Test
    void canTargetUntappedCreatureAndDoesNotUntapOthers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        other.tap();

        castCorridorMonitor(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
    }

    @Test
    void canTargetItselfAfterEntering() {
        harness.castFromHand(player1, new CorridorMonitor(), "{1}{U}");
        harness.passBothPriorities();
        Permanent monitor = findPermanent(player1, "Corridor Monitor");
        harness.handlePermanentChosen(player1, monitor.getId());
        monitor.tap();

        harness.passBothPriorities();

        assertThat(monitor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotUntapTargetThatOpponentNowControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        target.tap();
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castCorridorMonitor(Permanent target) {
        harness.setHand(player1, List.of(new CorridorMonitor()));
        addCorridorMonitorMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCorridorMonitorMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
