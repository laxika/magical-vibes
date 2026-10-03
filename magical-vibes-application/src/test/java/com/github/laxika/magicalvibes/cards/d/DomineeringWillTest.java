package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DomineeringWill.class, GrizzlyBears.class, ControlMagic.class, Disenchant.class})
class DomineeringWillTest extends BaseCardTest {

    @Test
    void gainsControlUntapsAndForcesAllChosenCreaturesToBlock() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();

        castDomineeringWill(first, second);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(first.isSummoningSick()).isTrue();
        assertThat(second.isSummoningSick()).isTrue();
        assertThat(first.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(second.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    void controlAndBlockRequirementExpireAtEndOfTurn() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        castDomineeringWill(first, second);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(first.getId(), second.getId());
        assertThat(first.isMustBlockThisTurnIfAble()).isFalse();
        assertThat(second.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    void cannotTargetAnAttackingCreature() {
        Permanent attacking = addCreatureReady(player1, new GrizzlyBears());
        attacking.setAttacking(true);
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(player2.getId(), attacking.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonattacking creature");
    }

    private void castDomineeringWill(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(player2.getId(), first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    @Test
    void canChooseNoCreatures() {
        Permanent untouched = addCreatureReady(player1, new GrizzlyBears());
        untouched.tap();
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c instanceof DomineeringWill);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(untouched);
        assertThat(untouched.isTapped()).isTrue();
        assertThat(untouched.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    void canTargetThreeCreaturesIncludingOneAlreadyControlledByRecipient() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        first.tap();
        second.tap();
        third.tap();
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0,
                List.of(player2.getId(), first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second, third);
        for (Permanent creature : List.of(first, second, third)) {
            assertThat(creature.isTapped()).isFalse();
            assertThat(creature.isMustBlockThisTurnIfAble()).isTrue();
        }
    }

    @Test
    void skipsCreatureThatBecomesAttackingBeforeResolution() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(player2.getId(), first.getId(), second.getId()));
        first.setAttacking(true);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(first.isTapped()).isTrue();
        assertThat(first.isMustBlockThisTurnIfAble()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
        assertThat(second.isTapped()).isFalse();
        assertThat(second.isMustBlockThisTurnIfAble()).isTrue();
    }

    @Test
    void maintainsRecipientsControlWhenOlderControlAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Control Magic");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.setHand(player1, List.of(new DomineeringWill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.isMustBlockThisTurnIfAble()).isFalse();
    }

    @Test
    void stolenCreaturesMustActuallyBlockAndCanChooseDifferentAttackers() {
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        castDomineeringWill(firstBlocker, secondBlocker);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block this turn if able");
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.getBlockingTargetIds()).containsExactly(secondAttacker.getId());
        assertThat(secondBlocker.getBlockingTargetIds()).containsExactly(firstAttacker.getId());
    }

    @Test
    void creatureTappedAfterResolutionIsNotForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        castDomineeringWill(first, second);
        first.tap();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isFalse();
        assertThat(second.getBlockingTargetIds()).containsExactly(attacker.getId());
    }
}
