package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismariPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack before its magecraft ability resolves")
    void cannotAttackBeforeMagecraft() {
        addCreatureReady(player1, new PrismariPledgemage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        beginAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Casting an instant lets Prismari Pledgemage attack this turn")
    void castingInstantAllowsAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        harness.passBothPriorities();

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(pledgemage.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Copying an instant lets Prismari Pledgemage attack this turn")
    void copyingInstantAllowsAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, pledgemage.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(pledgemage.isAttacking()).isTrue();
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
