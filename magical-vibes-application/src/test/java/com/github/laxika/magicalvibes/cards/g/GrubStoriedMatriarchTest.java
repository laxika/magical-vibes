package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MoggRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrubStoriedMatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and returns up to one targeted Goblin card from the graveyard")
    void entersAndReturnsTargetedGoblin() {
        MoggRaider goblin = new MoggRaider();
        harness.setGraveyard(player1, List.of(goblin));
        harness.setHand(player1, List.of(new GrubStoriedMatriarch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mogg Raider");
        harness.assertNotInGraveyard(player1, "Mogg Raider");
    }

    @Test
    @DisplayName("Pays red in the first main phase to transform into the back face")
    void paysRedToTransformToBackFace() {
        Permanent grub = addFrontFace(player1);

        advanceToFirstMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(grub.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforming back to the front face returns a targeted Goblin card")
    void transformingBackReturnsTargetedGoblin() {
        MoggRaider goblin = new MoggRaider();
        harness.setGraveyard(player1, List.of(goblin));
        Permanent grub = addBackFace(player1);

        advanceToFirstMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        assertThat(grub.isTransformed()).isFalse();
        harness.assertInHand(player1, "Mogg Raider");
        harness.assertNotInGraveyard(player1, "Mogg Raider");
    }

    @Test
    @DisplayName("Blighting a creature creates its tapped and attacking copy")
    void blightCreatesTappedAndAttackingCopy() {
        Permanent grub = addBackFace(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    private Permanent addFrontFace(Player player) {
        return addCreatureReady(player, new GrubStoriedMatriarch());
    }

    private Permanent addBackFace(Player player) {
        GrubStoriedMatriarch card = new GrubStoriedMatriarch();
        Permanent permanent = addCreatureReady(player, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void advanceToFirstMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
