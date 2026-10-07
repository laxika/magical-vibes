package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurnishedDunestomper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarkirDuneshaper.class, BurnishedDunestomper.class})
class TarkirDuneshaperTest extends BaseCardTest {

    @Test
    void transformsByPayingGreenMana() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duneshaper.isTransformed()).isTrue();
        assertThat(duneshaper.getCard()).isInstanceOf(BurnishedDunestomper.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duneshaper.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addDuneshaper();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotTransformDuringCombat() {
        addDuneshaper();
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateAgainWhileTransformationIsOnStack() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);

        assertThat(duneshaper.isTransformed()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(duneshaper.isTransformed()).isTrue();
    }

    @Test
    void cannotPayPhyrexianCostWithOnlyOneLife() {
        Permanent duneshaper = addDuneshaper();
        prepareMainPhase(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duneshaper.isTransformed()).isFalse();
        harness.assertLife(player1, 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformedCreatureTramplesOverAFrontFaceBlocker() {
        Permanent duneshaper = addDuneshaper();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TarkirDuneshaper());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        duneshaper.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Tarkir Duneshaper");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(duneshaper);
    }

    private Permanent addDuneshaper() {
        return harness.addToBattlefieldAndReturn(player1, new TarkirDuneshaper());
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
