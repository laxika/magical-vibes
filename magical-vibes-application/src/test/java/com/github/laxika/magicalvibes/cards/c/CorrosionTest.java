package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DiamondKaleidoscope;
import com.github.laxika.magicalvibes.cards.m.MagmaMine;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Corrosion.class, CreepingMold.class, DiamondKaleidoscope.class, MagmaMine.class,
        PhyrexianWalker.class, CloudElemental.class})
class CorrosionTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep rust trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new Corrosion());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Puts a rust counter on each artifact the targeted opponent controls")
    void putsRustOnTargetOpponentsArtifacts() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent oppArtifact = harness.addToBattlefieldAndReturn(player2, new DiamondKaleidoscope());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MagmaMine());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // rust trigger
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities(); // cumulative upkeep
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(oppArtifact.getCounterCount(CounterType.RUST)).isEqualTo(1);
        assertThat(ownArtifact.getCounterCount(CounterType.RUST)).isEqualTo(0);
    }

    @Test
    @DisplayName("Destroys artifacts once rust counters meet or exceed mana value")
    void destroysWhenRustMeetsManaValue() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new MagmaMine());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // rust + destroy
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mine);
        harness.assertInGraveyard(player2, "Magma Mine");
    }

    @Test
    @DisplayName("Does not destroy an artifact whose mana value still exceeds its rust counters")
    void doesNotDestroyWhenManaValueExceedsRust() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent kaleidoscope = harness.addToBattlefieldAndReturn(player2, new DiamondKaleidoscope());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kaleidoscope);
        assertThat(kaleidoscope.getCounterCount(CounterType.RUST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys own artifacts that have enough rust counters")
    void destroysOwnArtifactsWithRust() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new MagmaMine());
        own.setCounterCount(CounterType.RUST, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
        harness.assertInGraveyard(player1, "Magma Mine");
    }

    @Test
    @DisplayName("Leaving the battlefield removes all rust counters from all permanents")
    void leavesRemovesAllRustCounters() {
        Permanent corrosion = harness.addToBattlefieldAndReturn(player1, new Corrosion());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new MagmaMine());
        mine.setCounterCount(CounterType.RUST, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudElemental());
        creature.setCounterCount(CounterType.RUST, 2);

        harness.setHand(player2, List.of(new CreepingMold()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveSorcery(player2, 0, corrosion.getId());
        harness.passBothPriorities(); // LTB remove rust

        assertThat(mine.getCounterCount(CounterType.RUST)).isEqualTo(0);
        assertThat(creature.getCounterCount(CounterType.RUST)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not destroy a zero-mana artifact without rust counters")
    void doesNotDestroyZeroManaArtifactWithoutRust() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new PhyrexianWalker());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(walker);
        assertThat(walker.getCounterCount(CounterType.RUST)).isEqualTo(0);
    }

    @Test
    @DisplayName("Rust destruction cannot be replaced by regeneration")
    void rustDestructionCannotBeRegenerated() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new MagmaMine());
        mine.setRegenerationShield(1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mine);
        harness.assertInGraveyard(player2, "Magma Mine");
    }

    @Test
    @DisplayName("Cumulative upkeep cost increases with each age counter")
    void cumulativeUpkeepCostIncreasesWithAgeCounters() {
        Permanent corrosion = harness.addToBattlefieldAndReturn(player1, new Corrosion());
        corrosion.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(corrosion);
        harness.assertInGraveyard(player1, "Corrosion");
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Corrosion")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent corrosion = harness.addToBattlefieldAndReturn(player1, new Corrosion());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // rust
        harness.passBothPriorities(); // CU
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(corrosion);
        harness.assertInGraveyard(player1, "Corrosion");
    }
}
