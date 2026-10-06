package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GameTrailChangeling;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharedAnimosity.class, PricklyBoggart.class, ElvishWarrior.class, GameTrailChangeling.class,
        ImprisonedInTheMoon.class})
class SharedAnimosityTest extends BaseCardTest {

    @Test
    @DisplayName("Each attacker gets +1/+0 per other attacker sharing a creature type")
    void boostScalesWithSharedTypeAttackers() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        // Two Goblins attack: each has exactly one other attacker sharing a type -> +1/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin1.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost scales with three attackers sharing a creature type")
    void boostScalesWithThreeSharingAttackers() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin3 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        // Each Goblin sees two other sharing attackers -> +2/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(2);
        assertThat(goblin2.getPowerModifier()).isEqualTo(2);
        assertThat(goblin3.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The shared-type count is determined as each trigger resolves")
    void countsSharingAttackersAtResolution() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());
        Permanent removedGoblin = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2, 3));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, removedGoblin));
        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A permanent that is no longer a creature is not counted")
    void ignoresAttackingPermanentsThatAreNoLongerCreatures() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(goblin2.getId());
        assertThat(gqs.isCreature(gd, goblin2)).isFalse();

        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isZero();
        assertThat(goblin2.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("No boost when no other attacker shares a creature type")
    void noBoostWithoutSharedType() {
        addSharedAnimosity();
        Permanent goblin = addCreatureReady(player1, new PricklyBoggart());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(elf.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only attackers sharing a type with the triggering creature are counted")
    void countsOnlySharingAttackers() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        // Each Goblin sees one other Goblin (the Elf doesn't count) -> +1/+0.
        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
        // The Elf shares with nobody -> no boost.
        assertThat(elf.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A Changeling attacker shares a creature type with every other attacker")
    void changelingSharesWithEveryAttacker() {
        addSharedAnimosity();
        Permanent changeling = addCreatureReady(player1, new GameTrailChangeling());
        Permanent goblin = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        // Changeling has every creature type, so it shares with the Goblin and vice versa -> +1 each.
        assertThat(changeling.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblin1.getPowerModifier()).isZero();
        assertThat(goblin2.getPowerModifier()).isZero();
    }

    private void addSharedAnimosity() {
        harness.addToBattlefield(player1, new SharedAnimosity());
    }

    @Test
    @DisplayName("Nonattacking creatures on either battlefield neither count nor receive a boost")
    void ignoresNonattackingCreatures() {
        addSharedAnimosity();
        Permanent attacker = addCreatureReady(player1, new PricklyBoggart());
        Permanent ally = addCreatureReady(player1, new PricklyBoggart());
        Permanent opponent = addCreatureReady(player2, new PricklyBoggart());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(ally.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's Shared Animosity does not boost your attackers")
    void onlyTriggersForItsControllersAttackers() {
        harness.addToBattlefield(player2, new SharedAnimosity());
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isZero();
        assertThat(goblin2.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Multiple Shared Animosities each boost every matching attacker")
    void multipleCopiesStackTheirBoosts() {
        addSharedAnimosity();
        addSharedAnimosity();
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(2, 3));
        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isEqualTo(2);
        assertThat(goblin2.getPowerModifier()).isEqualTo(2);
        assertThat(goblin1.getToughnessModifier()).isZero();
        assertThat(goblin2.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Removing Shared Animosity does not stop its pending boosts")
    void pendingTriggersSurviveSourceRemoval() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new SharedAnimosity());
        Permanent goblin1 = addCreatureReady(player1, new PricklyBoggart());
        Permanent goblin2 = addCreatureReady(player1, new PricklyBoggart());

        declareAttackers(List.of(1, 2));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));
        resolveAllTriggers();

        assertThat(goblin1.getPowerModifier()).isEqualTo(1);
        assertThat(goblin2.getPowerModifier()).isEqualTo(1);
    }
}
