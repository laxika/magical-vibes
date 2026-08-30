package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncisorGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Incisor Glider boosts creatures you control when an opponent has three poison counters")
    void attackBoostsOwnCreaturesWhenOpponentHasThreePoisonCounters() {
        Permanent glider = new Permanent(new IncisorGlider());
        glider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(glider);

        Permanent otherCreature = new Permanent(new EliteVanguard());
        otherCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherCreature);

        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(glider.getPowerModifier()).isEqualTo(1);
        assertThat(glider.getToughnessModifier()).isEqualTo(1);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(1);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with Incisor Glider does not boost creatures when no opponent has three poison counters")
    void attackDoesNotBoostWithoutThreePoisonCounters() {
        Permanent glider = new Permanent(new IncisorGlider());
        glider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(glider);

        Permanent otherCreature = new Permanent(new EliteVanguard());
        otherCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherCreature);

        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(glider.getPowerModifier()).isEqualTo(0);
        assertThat(glider.getToughnessModifier()).isEqualTo(0);
        assertThat(otherCreature.getPowerModifier()).isEqualTo(0);
        assertThat(otherCreature.getToughnessModifier()).isEqualTo(0);
    }
}
