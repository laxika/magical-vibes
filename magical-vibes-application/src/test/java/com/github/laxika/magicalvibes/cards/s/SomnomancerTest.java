package com.github.laxika.magicalvibes.cards.s;

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

class SomnomancerTest extends BaseCardTest {

    private void castSomnomancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Somnomancer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Accepting the may taps the target creature")
    void tapsTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.isTapped()).isFalse();

        castSomnomancer();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the may leaves the creature untapped")
    void decliningLeavesUntapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();

        castSomnomancer();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving triggers the may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSomnomancer();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
