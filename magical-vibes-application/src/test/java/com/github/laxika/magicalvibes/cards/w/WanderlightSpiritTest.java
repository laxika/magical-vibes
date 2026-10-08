package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WanderlightSpirit.class, AirElemental.class, GrizzlyBears.class})
class WanderlightSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Wanderlight Spirit can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent spirit = addReadyBlocker();
        addAttackingCreature(new AirElemental());

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wanderlight Spirit cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addReadyBlocker();
        addAttackingCreature(new GrizzlyBears());

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Wanderlight Spirit can block a ground creature that has gained flying")
    void canBlockCreatureWithGrantedFlying() {
        Permanent spirit = addReadyBlocker();
        Permanent attacker = addAttackingCreature(new GrizzlyBears());
        attacker.getGrantedKeywords().add(Keyword.FLYING);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Wanderlight Spirit cannot block a flying creature that has lost flying")
    void cannotBlockCreatureWithRemovedFlying() {
        addReadyBlocker();
        Permanent attacker = addAttackingCreature(new AirElemental());
        attacker.getRemovedKeywords().add(Keyword.FLYING);

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private Permanent addReadyBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WanderlightSpirit());
        blocker.setSummoningSick(false);
        return blocker;
    }

    private Permanent addAttackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
