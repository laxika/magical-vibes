package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OldManOfTheSea.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class OldManOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is at most Old Man of the Sea's power")
    void gainsControlWithinPower() {
        Permanent oldMan = addReadyCreature(player1, new OldManOfTheSea());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(oldMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose power exceeds Old Man of the Sea's power")
    void rejectsCreatureAbovePower() {
        Permanent oldMan = addReadyCreature(player1, new OldManOfTheSea());
        Permanent target = addReadyCreature(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, oldMan), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    @Test
    @DisplayName("Losing the power condition ends control")
    void losesControlWhenTargetBecomesTooPowerful() {
        Permanent oldMan = addReadyCreature(player1, new OldManOfTheSea());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Untapping Old Man of the Sea ends control")
    void untappingEndsControl() {
        Permanent oldMan = addReadyCreature(player1, new OldManOfTheSea());
        Permanent target = addReadyCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oldMan.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
