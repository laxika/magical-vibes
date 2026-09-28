package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmovableRod.class, GrizzlyBears.class})
class ImmovableRodTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Immovable Rod makes its controller venture into the dungeon")
    void untappingVentureIntoDungeon() {
        Permanent rod = addReadyRod(player1);
        rod.tap();

        runUntapStep(player1);
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isFalse();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("The activated ability removes abilities and prevents attacking and blocking")
    void activatedAbilityLocksTargetPermanent() {
        Permanent rod = addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isTrue();
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The activated ability's restriction ends when Immovable Rod untaps")
    void restrictionEndsWhenRodUntaps() {
        Permanent rod = addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        runUntapStep(player1);
        harness.passBothPriorities();

        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The activated ability cannot target Immovable Rod itself")
    void cannotTargetItself() {
        Permanent rod = addReadyRod(player1);
        addRodMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rod.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent");
    }

    private Permanent addReadyRod(Player player) {
        Permanent rod = harness.addToBattlefieldAndReturn(player, new ImmovableRod());
        rod.setSummoningSick(false);
        return rod;
    }

    private Permanent addCreatureReady(Player player, GrizzlyBears card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void addRodMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void declareAttack(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, java.util.List.of(index));
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(untappingPlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(untappingPlayer, true);
    }
}
