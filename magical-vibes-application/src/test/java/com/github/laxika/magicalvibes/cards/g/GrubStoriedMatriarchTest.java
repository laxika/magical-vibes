package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrubStoriedMatriarch.class, GanglyStompling.class})
class GrubStoriedMatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and returns up to one targeted Goblin card from the graveyard")
    void entersAndReturnsTargetedGoblin() {
        GanglyStompling goblin = new GanglyStompling();
        harness.setGraveyard(player1, List.of(goblin));
        harness.setHand(player1, List.of(new GrubStoriedMatriarch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(goblin.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gangly Stompling");
        harness.assertNotInGraveyard(player1, "Gangly Stompling");
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
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Transforming back to the front face returns a targeted Goblin card")
    void transformingBackReturnsTargetedGoblin() {
        GanglyStompling goblin = new GanglyStompling();
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
        harness.assertInHand(player1, "Gangly Stompling");
        harness.assertNotInGraveyard(player1, "Gangly Stompling");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Blighting a creature creates its tapped and attacking copy")
    void blightCreatesTappedAndAttackingCopy() {
        addBackFace(player1);
        Permanent target = addCreatureReady(player1, new GanglyStompling());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Gangly Stompling");
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseNoGoblinToReturn() {
        GanglyStompling goblin = new GanglyStompling();
        harness.setGraveyard(player1, List.of(goblin));
        harness.setHand(player1, List.of(new GrubStoriedMatriarch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gangly Stompling");
        harness.assertNotInHand(player1, "Gangly Stompling");
    }

    @Test
    void decliningRedPaymentLeavesFrontFaceAndManaUnchanged() {
        Permanent grub = addFrontFace(player1);
        advanceToFirstMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(grub.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void decliningBlackPaymentLeavesBackFaceAndManaUnchanged() {
        Permanent grub = addBackFace(player1);
        advanceToFirstMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(grub.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void doesNotTransformAtOpponentsFirstMainPhase() {
        Permanent grub = addFrontFace(player1);
        advanceToFirstMainPhase(player2);

        assertThat(grub.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningBlightCreatesNoTokenOrCounters() {
        addBackFace(player1);
        Permanent creature = addCreatureReady(player1, new GanglyStompling());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesCreatureBeforeLethalBlightKillsIt() {
        addBackFace(player1);
        Permanent creature = addCreatureReady(player1, new GanglyStompling());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gangly Stompling");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Gangly Stompling"));
    }

    @Test
    void tokenRemainsUntilItsEndStepSacrificeTriggerResolves() {
        addBackFace(player1);
        Permanent creature = addCreatureReady(player1, new GanglyStompling());
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).isNotEmpty();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
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
