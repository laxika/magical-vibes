package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessOpponentDealtDamageThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodcrazedGoblinTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC CantAttackUnlessOpponentDealtDamageThisTurnEffect")
    void hasCorrectStructure() {
        BloodcrazedGoblin card = new BloodcrazedGoblin();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackUnlessOpponentDealtDamageThisTurnEffect.class);
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Cannot attack when no opponent has been dealt damage this turn")
    void cannotAttackWhenNoOpponentDealtDamage() {
        addCreatureReady(player1, new BloodcrazedGoblin());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when an opponent has been dealt damage this turn")
    void canAttackWhenOpponentDealtDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.playersDealtDamageThisTurn.add(player2.getId());

        declareAttackers(player1, List.of(0));

        // Attack went through — opponent takes combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when only the controller has been dealt damage")
    void cannotAttackWhenOnlyControllerDealtDamage() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.playersDealtDamageThisTurn.add(player1.getId());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restriction is cleared at the start of a new turn")
    void restrictionClearedOnNewTurn() {
        addCreatureReady(player1, new BloodcrazedGoblin());
        gd.playersDealtDamageThisTurn.add(player2.getId());

        // Simulate new turn clearing the tracker
        gd.playersDealtDamageThisTurn.clear();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
