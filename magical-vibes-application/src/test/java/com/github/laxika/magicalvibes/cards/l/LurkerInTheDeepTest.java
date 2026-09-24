package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkerInTheDeep.class, Island.class, GrizzlyBears.class})
class LurkerInTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Entering seeks a nonland card and manifests a conjured duplicate")
    void enteringSeeksAndManifestsDuplicate() {
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Island"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Attacking seeks a nonland card")
    void attackingSeeks() {
        addCreatureReady(player1, new LurkerInTheDeep());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Impending enters as an enchantment and becomes a creature after its last time counter")
    void impendingBecomesCreatureAfterLastTimeCounter() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent lurker = findPermanent(player1, "Lurker in the Deep");
        assertThat(lurker.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, lurker)).isFalse();

        lurker.setCounterCount(CounterType.TIME, 1);
        advanceToOwnEndStep();

        assertThat(lurker.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, lurker)).isTrue();
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
