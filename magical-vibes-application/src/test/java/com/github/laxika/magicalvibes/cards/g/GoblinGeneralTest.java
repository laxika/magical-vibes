package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Goblin General boosts Goblins you control including itself")
    void attackBoostsGoblins() {
        Permanent general = new Permanent(new GoblinGeneral());
        general.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(general);

        Permanent otherGoblin = new Permanent(new GoblinPiker());
        otherGoblin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherGoblin);

        // Give player2 a playable instant to prevent auto-pass
        harness.setHand(player2, List.of(new GoblinPiker()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Goblin General is a Goblin, so it boosts itself too
        assertThat(general.getPowerModifier()).isEqualTo(1);
        assertThat(general.getToughnessModifier()).isEqualTo(1);
        assertThat(otherGoblin.getPowerModifier()).isEqualTo(1);
        assertThat(otherGoblin.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Goblin General does not boost non-Goblin creatures")
    void attackDoesNotBoostNonGoblins() {
        Permanent general = new Permanent(new GoblinGeneral());
        general.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(general);

        // Grizzly Bears is a Bear, not a Goblin
        Permanent bears = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player2, List.of(new GoblinPiker()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }
}
