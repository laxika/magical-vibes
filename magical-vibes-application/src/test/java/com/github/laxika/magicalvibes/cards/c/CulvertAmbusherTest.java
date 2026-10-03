package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TopiaryPanther;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({CulvertAmbusher.class, TopiaryPanther.class})
class CulvertAmbusherTest extends BaseCardTest {

    @Test
    void enteringFaceUpForcesTargetCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new TopiaryPanther());
        Permanent target = addCreatureReady(player2, new TopiaryPanther());
        castFaceUp();

        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    void turningFaceUpForcesTargetCreatureToBlock() {
        Permanent attacker = addCreatureReady(player1, new TopiaryPanther());
        Permanent target = addCreatureReady(player2, new TopiaryPanther());
        harness.setHand(player1, List.of(new CulvertAmbusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent ambusher = findPermanent(player1, "Culvert Ambusher");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(ambusher));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    private void castFaceUp() {
        harness.setHand(player1, List.of(new CulvertAmbusher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }

    @Test
    void enteringFaceDownDoesNotForceAnyCreatureToBlock() {
        Permanent target = addCreatureReady(player2, new TopiaryPanther());
        castFaceDown();

        assertThat(findPermanent(player1, "Culvert Ambusher").isFaceDown()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    void tappedTargetIsNotRequiredToBlock() {
        Permanent attacker = addCreatureReady(player1, new TopiaryPanther());
        Permanent target = addCreatureReady(player2, new TopiaryPanther());
        target.setTapped(true);
        castFaceUp();
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    void disguisedCreatureCountersOpponentsTargetedAbilityWhenWardCannotBePaid() {
        castFaceDown();
        Permanent disguised = findPermanent(player1, "Culvert Ambusher");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CulvertAmbusher()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player2, disguised.getId());
        resolveAllTriggers();

        assertThat(disguised.isMustBlockThisTurnIfAble()).isFalse();
    }

    private void castFaceDown() {
        harness.setHand(player1, List.of(new CulvertAmbusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
    }
}
