package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HungerOfTheHowlpack;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeguilerOfWills.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class,
        EvolvingWilds.class, HungerOfTheHowlpack.class, TragicSlip.class})
class BeguilerOfWillsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BeguilerOfWills()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(BeguilerOfWills.class);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BeguilerOfWills()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Beguiler of Wills");
    }

    @Test
    @DisplayName("Steals creature with power equal to number of controlled creatures")
    void stealsCreatureWithPowerEqualToCreatureCount() {
        // Beguiler + GrizzlyBears = 2 creatures; target GrizzlyBears (power 2) on opponent side
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Steals creature with power less than number of controlled creatures")
    void stealsCreatureWithPowerLessThanCreatureCount() {
        // Beguiler + GrizzlyBears = 2 creatures; target LlanowarElves (power 1)
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can steal own creature")
    void canStealOwnCreature() {
        // Beguiler + target bears = 2 creatures; bears has power 2
        addCreatureReady(player1, new BeguilerOfWills());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        // Targeting own creature is legal
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // Should still be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target creature with power greater than number of controlled creatures")
    void cannotTargetCreatureWithTooMuchPower() {
        // Beguiler alone = 1 creature; cannot target GrizzlyBears (power 2)
        addCreatureReady(player1, new BeguilerOfWills());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adding more creatures enables targeting higher-power creatures")
    void addingCreaturesEnablesHigherPowerTargets() {
        // Start with Beguiler + 2 GrizzlyBears = 3 creatures; can target HillGiant (power 3)
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());

        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating ability taps Beguiler of Wills")
    void activatingTapsBeguiler() {
        Permanent beguiler = addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(beguiler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when summoning sick")
    void cannotActivateWhenSummoningSick() {
        Permanent beguiler = harness.addToBattlefieldAndReturn(player1, new BeguilerOfWills());
        beguiler.setSummoningSick(true);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent beguiler = addCreatureReady(player1, new BeguilerOfWills());
        beguiler.tap();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Stolen creature is tracked in stolenCreatures map")
    void stolenCreatureIsTracked() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stolenCreatures).containsEntry(target.getId(), player2.getId());
    }

    @Test
    @DisplayName("Stolen creature has summoning sickness")
    void stolenCreatureHasSummoningSickness() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(BeguilerOfWills.class);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Stealing adds to game log")
    void stealingAddsToGameLog() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("gains control of") && log.contains("Llanowar Elves"));
    }


    @Test
    @DisplayName("Losing a creature in response can make the target illegal")
    void losingCreatureInResponseMakesTargetIllegal() {
        addCreatureReady(player1, new BeguilerOfWills());
        Permanent support = addCreatureReady(player1, new LlanowarElves());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, support.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(support);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Increasing target power in response can make it illegal")
    void increasingTargetPowerInResponseMakesTargetIllegal() {
        addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new HungerOfTheHowlpack()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Ability still resolves after Beguiler dies if the target remains legal")
    void sourceLeavingBeforeResolutionDoesNotCounterAbility() {
        Permanent beguiler = addCreatureReady(player1, new BeguilerOfWills());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castInstant(player2, 0, beguiler.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beguiler);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Control persists after Beguiler dies and across turns")
    void controlPersistsAfterSourceLeavesAndTurnEnds() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(new EvolvingWilds()));
        Permanent beguiler = addCreatureReady(player1, new BeguilerOfWills());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, beguiler.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(beguiler);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Control persists when target power later exceeds the creature count")
    void controlPersistsWhenTargetLaterGrowsTooLarge() {
        addCreatureReady(player1, new BeguilerOfWills());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player2, List.of(new HungerOfTheHowlpack(), new HungerOfTheHowlpack()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("A stolen creature enables the next activation to target greater power")
    void stolenCreatureCountsForSubsequentActivation() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        harness.setLibrary(player2, List.of(new EvolvingWilds()));
        Permanent beguiler = addCreatureReady(player1, new BeguilerOfWills());
        Permanent firstTarget = addCreatureReady(player2, new LlanowarElves());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(beguiler.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstTarget, secondTarget);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstTarget, secondTarget);
    }

    @Test
    @DisplayName("The ability counts creatures controlled by its activating player")
    void countsActivatingPlayersCreatures() {
        addCreatureReady(player2, new BeguilerOfWills());
        addCreatureReady(player2, new LlanowarElves());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }
}
