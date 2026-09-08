package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeiledSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Serpent creature when an opponent casts a spell")
    void becomesSerpentCreatureWhenOpponentCastsSpell() {
        Permanent serpent = transformSerpent();

        assertThat(gqs.isCreature(gd, serpent)).isTrue();
        assertThat(gqs.isEnchantment(gd, serpent)).isFalse();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, serpent)).containsExactly(CardSubtype.SERPENT);
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerForControllerCast() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new VeiledSerpent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);

        assertThat(gqs.isEnchantment(gd, serpent)).isTrue();
        assertThat(gqs.isCreature(gd, serpent)).isFalse();
    }

    @Test
    @DisplayName("Cannot attack unless the defending player controls an Island")
    void cannotAttackWithoutDefendingIsland() {
        Permanent serpent = transformSerpent();
        serpent.setSummoningSick(false);
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when the defending player controls an Island")
    void canAttackWithDefendingIsland() {
        Permanent serpent = transformSerpent();
        serpent.setSummoningSick(false);
        harness.addToBattlefield(player2, new Island());
        beginAttackers();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(serpent.isAttacking()).isTrue();
    }

    private Permanent transformSerpent() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player1, new VeiledSerpent());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        return serpent;
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
