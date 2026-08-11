package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(battlemage.isTapped()).isTrue();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the cast trigger leaves the Battlemage tapped")
    void decliningCastTriggerLeavesBattlemageTapped() {
        Permanent battlemage = addReadyBattlemage();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(battlemage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        addReadyBattlemage();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBattlemage() {
        Permanent battlemage = new Permanent(new LeoninBattlemage());
        battlemage.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(battlemage);
        return battlemage;
    }
}
