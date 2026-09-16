package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FilthyCur;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhisperingShade.class, FilthyCur.class, Swamp.class})
class WhisperingShadeTest extends BaseCardTest {

    @Test
    @DisplayName("{B} gives Whispering Shade +1/+1 until end of turn")
    void blackManaAbilityBoostsShade() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Whispering Shade's boosts stack")
    void boostsStack() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(3);
    }

    @Test
    @DisplayName("Whispering Shade's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
    }

    @Test
    @DisplayName("Swampwalk prevents blocking while the defending player controls a Swamp")
    void swampwalkPreventsBlockerWhenDefenderControlsSwamp() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        Permanent blocker = addCreatureReady(player2, new FilthyCur());
        harness.addToBattlefield(player2, new Swamp());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, shade))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("swampwalk");
    }

    @Test
    @DisplayName("Swampwalk allows blocking when the defending player controls no Swamp")
    void swampwalkAllowsBlockerWithoutSwamp() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        Permanent blocker = addCreatureReady(player2, new FilthyCur());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        declareBlock(blocker, shade);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Swampwalk does not care about Swamps controlled by the attacking player")
    void attackingPlayersSwampDoesNotEnableSwampwalk() {
        Permanent shade = addCreatureReady(player1, new WhisperingShade());
        Permanent blocker = addCreatureReady(player2, new FilthyCur());
        harness.addToBattlefield(player1, new Swamp());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        declareBlock(blocker, shade);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
