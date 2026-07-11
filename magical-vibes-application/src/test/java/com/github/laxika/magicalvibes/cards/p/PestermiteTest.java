package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PestermiteTest extends BaseCardTest {

    private void castPestermite() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Pestermite()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Resolving Pestermite triggers the may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting may prompts for target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting and choosing an untapped permanent taps it")
    void tapsUntappedTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, target.getId()); // choose target -> resolves inline

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting and choosing a tapped permanent untaps it")
    void untapsTappedTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        target.tap();
        assertThat(target.isTapped()).isTrue();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, target.getId()); // choose target -> resolves inline

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target any permanent, including a land")
    void canTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent land = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(land.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> permanent choice
        harness.handlePermanentChosen(player1, land.getId()); // choose target -> resolves inline

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the may leaves the permanent unchanged")
    void decliningLeavesUnchanged() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.isTapped()).isFalse();

        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pestermite enters the battlefield")
    void pestermiteEntersBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pestermite"));
    }

    @Test
    @DisplayName("ETB trigger uses the triggered-ability stack entry")
    void etbTriggerType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castPestermite();
        harness.passBothPriorities(); // resolve creature spell -> may on stack

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pestermite");
    }
}
