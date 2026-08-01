package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent oppArtifact = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

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
        Permanent thopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // rust + destroy
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(thopter);
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Does not destroy an artifact whose mana value still exceeds its rust counters")
    void doesNotDestroyWhenManaValueExceedsRust() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new HowlingMine());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mine);
        assertThat(mine.getCounterCount(CounterType.RUST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys own artifacts that have enough rust counters")
    void destroysOwnArtifactsWithRust() {
        harness.addToBattlefield(player1, new Corrosion());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        own.setCounterCount(CounterType.RUST, 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Leaving the battlefield removes all rust counters from all permanents")
    void leavesRemovesAllRustCounters() {
        Permanent corrosion = harness.addToBattlefieldAndReturn(player1, new Corrosion());
        Permanent mine = harness.addToBattlefieldAndReturn(player2, new HowlingMine());
        mine.setCounterCount(CounterType.RUST, 3);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, corrosion.getId());
        harness.passBothPriorities(); // Disenchant
        harness.passBothPriorities(); // LTB remove rust

        assertThat(mine.getCounterCount(CounterType.RUST)).isEqualTo(0);
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
