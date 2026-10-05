package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiltrationLens.class, Memnite.class})
class InfiltrationLensTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches Infiltration Lens to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lens = addLens(player1);
        Permanent creature = addCreatureReady(player1, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lens.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by one creature, one trigger is created")
    void singleBlockerCreatesOneTrigger() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isEqualTo(1);
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by two creatures, two triggers are created")
    void multipleBlockersCreateMultipleTriggers() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());
        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting the may ability draws two cards")
    void acceptingMayAbilityDrawsTwoCards() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Declining the may ability does not draw cards")
    void decliningMayAbilityDoesNotDraw() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the MayEffect trigger
        harness.passBothPriorities();
        // Decline the "you may draw two cards" prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when unequipped creature is blocked")
    void noTriggerWhenNotEquipped() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        addLens(player1); // Lens on battlefield but not attached
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isZero();
    }

    @Test
    @DisplayName("Two blockers: accepting both may abilities draws four cards total")
    void twoBlockersAcceptBothDrawsFourCards() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());
        addCreatureReady(player2, new Memnite());

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 4);
    }

    @Test
    @DisplayName("Trigger is a triggered ability with correct source")
    void triggerHasCorrectMetadata() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Infiltration Lens")
                        && se.getSourcePermanentId().equals(lens.getId())
                        && se.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Lens controller controls the trigger even when the equipped creature is controlled by an opponent")
    void lensControllerControlsDrawTrigger() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player2);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        addCreatureReady(player2, new Memnite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getSourcePermanentId()).isEqualTo(lens.getId());
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
        });
        int lensControllerHand = gd.playerHands.get(player2.getId()).size();
        int creatureControllerHand = gd.playerHands.get(player1.getId()).size();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(lensControllerHand + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(creatureControllerHand);
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent lens = addLens(player1);
        Permanent creature = addCreatureReady(player2, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lens.isAttached()).isFalse();
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent lens = addLens(player1);
        Permanent creature = addCreatureReady(player1, new Memnite());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(lens.isAttached()).isFalse();
    }

    @Test
    void triggerStillDrawsAfterLensLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new Memnite());
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        addCreatureReady(player2, new Memnite());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        gd.playerBattlefields.get(player1.getId()).remove(lens);
        gd.playerGraveyards.get(player1.getId()).add(lens.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    private Permanent addLens(Player player) {
        return harness.addToBattlefieldAndReturn(player, new InfiltrationLens());
    }
}
