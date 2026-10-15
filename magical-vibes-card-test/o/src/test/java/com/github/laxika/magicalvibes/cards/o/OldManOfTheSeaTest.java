package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({OldManOfTheSea.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class, MishrasFactory.class})
class OldManOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is at most Old Man of the Sea's power")
    void gainsControlWithinPower() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(oldMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose power exceeds Old Man of the Sea's power")
    void rejectsCreatureAbovePower() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, oldMan), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power less than or equal to Old Man of the Sea's power");
    }

    @Test
    @DisplayName("Losing the power condition ends control")
    void losesControlWhenTargetBecomesTooPowerful() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Untapping Old Man of the Sea ends control")
    void untappingEndsControl() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(oldMan.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Declining untap preserves control")
    void decliningUntapPreservesControl() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(oldMan.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("A target that becomes too powerful before resolution is not stolen")
    void targetPowerIsCheckedAgainOnResolution() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(oldMan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control continues when the stolen permanent stops being a creature")
    void controlContinuesAfterFactoryAnimationEnds() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent factory = addCreatureReady(player2, new MishrasFactory());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, battlefieldIndex(player2, factory), 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, factory.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(factory);

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(gqs.isCreature(gd, factory)).isFalse();
        assertThat(oldMan.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(factory);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(factory);
    }

    @Test
    @DisplayName("Changing Old Man's controller does not end its control effect")
    void changingSourceControllerPreservesControl() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent otherOldMan = addCreatureReady(player2, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, battlefieldIndex(player2, otherOldMan), null, oldMan.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(oldMan);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("An ended power-dependent control effect does not resume when power returns")
    void endedControlDoesNotResumeAfterGrowthExpires() {
        Permanent oldMan = addCreatureReady(player1, new OldManOfTheSea());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(player1, oldMan), null, target.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(oldMan.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
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
