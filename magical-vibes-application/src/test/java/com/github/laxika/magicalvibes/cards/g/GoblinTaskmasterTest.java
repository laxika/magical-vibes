package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTaskmaster.class, SkirkProspector.class, ElvishWarrior.class})
class GoblinTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives a target Goblin +1/+0 until end of turn")
    void boostsTargetGoblin() {
        addTaskmaster();
        Permanent goblin = addGoblin(player1);
        int originalPower = goblin.getEffectivePower();
        int originalToughness = goblin.getEffectiveToughness();

        activateTaskmaster(goblin.getId());

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower + 1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Ability can target an opponent's Goblin")
    void boostsOpponentsGoblin() {
        addTaskmaster();
        Permanent goblin = addGoblin(player2);
        int originalPower = goblin.getEffectivePower();

        activateTaskmaster(goblin.getId());

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addTaskmaster();
        Permanent goblin = addGoblin(player1);
        int originalPower = goblin.getEffectivePower();

        activateTaskmaster(goblin.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(goblin.getEffectivePower()).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("Ability cannot target a non-Goblin creature")
    void rejectsNonGoblinTarget() {
        addTaskmaster();
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, warrior.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Morph casts Goblin Taskmaster face down and turns it face up for {R}")
    void morphsFaceDownAndTurnsFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new GoblinTaskmaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent taskmaster = findPermanent(player1, "Goblin Taskmaster");
        assertThat(taskmaster.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(taskmaster));
        harness.passBothPriorities();

        assertThat(taskmaster.isFaceDown()).isFalse();
    }

    private void addTaskmaster() {
        addCreatureReady(player1, new GoblinTaskmaster());
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private Permanent addGoblin(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SkirkProspector());
    }

    private void activateTaskmaster(UUID targetId) {
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
    }
}
