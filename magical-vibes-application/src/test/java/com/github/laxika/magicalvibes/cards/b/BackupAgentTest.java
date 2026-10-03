package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HaloScarab;
import com.github.laxika.magicalvibes.cards.c.CitizensCrowbar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackupAgent.class, HaloScarab.class, CitizensCrowbar.class})
class BackupAgentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HaloScarab());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can put a +1/+1 counter on an opponent's creature")
    void etbPutsCounterOnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloScarab());

        cast(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CitizensCrowbar());
        harness.setHand(player1, List.of(new BackupAgent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself with its mandatory ability")
    void canTargetItselfOnEmptyBattlefield() {
        harness.castFromHand(player1, new BackupAgent(), "{1}{W}");
        harness.passBothPriorities();

        Permanent agent = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, agent.getId());
        harness.passBothPriorities();

        assertThat(agent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entering without being cast still triggers and can target an opposing creature")
    void enteringWithoutCastingTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloScarab());
        harness.enterBattlefieldAndReturn(player1, new BackupAgent());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability does not put a counter on a target that has left the battlefield")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloScarab());
        harness.enterBattlefieldAndReturn(player1, new BackupAgent());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even after Backup Agent leaves the battlefield")
    void sourceLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloScarab());
        Permanent agent = harness.enterBattlefieldAndReturn(player1, new BackupAgent());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(agent);
        harness.setGraveyard(player1, List.of(agent.getCard()));

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new BackupAgent()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
