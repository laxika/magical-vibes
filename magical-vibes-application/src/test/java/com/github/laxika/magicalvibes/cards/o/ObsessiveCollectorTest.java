package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObsessiveCollector.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, Shock.class})
class ObsessiveCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks a card whose mana value equals the controller's hand size")
    void seeksCardMatchingHandSize() {
        GrizzlyBears sought = new GrizzlyBears();
        Forest handForest = new Forest();
        GiantGrowth handGrowth = new GiantGrowth();
        GiantGrowth different = new GiantGrowth();
        harness.setHand(player1, List.of(handForest, handGrowth));
        harness.setLibrary(player1, List.of(different, sought));
        addAttackingCollector();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(different);
    }

    @Test
    @DisplayName("Does not seek a card when no card has the controller's hand-size mana value")
    void doesNotSeekNonmatchingCard() {
        Forest handForest = new Forest();
        GiantGrowth handGrowth = new GiantGrowth();
        GiantGrowth libraryCard = new GiantGrowth();
        harness.setHand(player1, List.of(handForest, handGrowth));
        harness.setLibrary(player1, List.of(libraryCard));
        addAttackingCollector();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handForest, handGrowth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when its controller does not pay")
    void wardCountersUnpaidSpell() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new ObsessiveCollector());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, collector.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(collector);
    }

    private Permanent addAttackingCollector() {
        Permanent collector = addCreatureReady(player1, new ObsessiveCollector());
        collector.setAttacking(true);
        return collector;
    }
}
