package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.n.Nourish;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninBattlemage.class, CrazedGoblin.class, Nourish.class})
class LeoninBattlemageTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("The activated ability boosts the target creature until end of turn")
    void boostsTargetCreature() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(battlemage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a spell offers to untap the Battlemage")
    void castingSpellMayUntapBattlemage() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(battlemage.isTapped()).isTrue();

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the cast trigger leaves the Battlemage tapped")
    void decliningCastTriggerLeavesBattlemageTapped() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a noncreature spell offers to untap the Battlemage")
    void castingNoncreatureSpellMayUntapBattlemage() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(battlemage.isTapped()).isTrue();

        harness.castFromHand(player1, new Nourish(), "{G}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's spell does not trigger the untap ability")
    void opponentsSpellDoesNotUntapBattlemage() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(battlemage.isTapped()).isTrue();

        harness.castFromHand(player2, new Nourish(), "{G}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated boost wears off at end of turn")
    void activatedBoostWearsOffAtEndOfTurn() {
        addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new CrazedGoblin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addReadyBattlemage();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBattlemage() {
        return addCreatureReady(player1, new LeoninBattlemage());
    }
}
