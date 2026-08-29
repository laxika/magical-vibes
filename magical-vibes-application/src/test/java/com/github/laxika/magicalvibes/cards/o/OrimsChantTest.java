package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrimsChantTest extends BaseCardTest {

    @Test
    @DisplayName("The target player can't cast spells for the rest of the turn")
    void targetPlayerCantCastSpells() {
        castChant(false);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without kicker, creatures can attack")
    void withoutKickerCreaturesCanAttack() {
        Permanent bear = addReadyCreature(player1);
        castChant(false);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, bear));
    }

    @Test
    @DisplayName("With kicker, all creatures can't attack, including one entering later")
    void withKickerCreaturesCantAttack() {
        Permanent existingBear = addReadyCreature(player1);
        castChant(true);
        Permanent laterBear = addReadyCreature(player2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .doesNotContain(indexOf(player1, existingBear));
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .doesNotContain(indexOf(player2, laterBear));
    }

    @Test
    @DisplayName("The kicker costs an additional white mana")
    void kickerRequiresAdditionalWhiteMana() {
        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The kicked attack restriction clears at the next turn")
    void kickedRestrictionClearsAtTurnTransition() {
        Permanent bear = addReadyCreature(player2);
        castChant(true);

        advanceTurn();

        assertThat(gd.creaturesCantAttackThisTurn).isFalse();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId()))
                .contains(indexOf(player2, bear));
    }

    private void castChant(boolean kicked) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OrimsChant()));
        harness.addMana(player1, ManaColor.WHITE, kicked ? 2 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, player2.getId());
        } else {
            harness.castInstant(player1, 0, player2.getId());
        }
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player) {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bear);
        return bear;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
