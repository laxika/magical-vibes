package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObsessiveCollector.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, Shock.class,
        ProdigalPyromancer.class})
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

    @Test
    @DisplayName("An empty hand seeks a land with mana value zero")
    void emptyHandSeeksLand() {
        Forest sought = new Forest();
        GiantGrowth different = new GiantGrowth();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(different, sought));
        addAttackingCollector();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(different);
    }

    @Test
    @DisplayName("Hand size is evaluated when the combat damage trigger resolves")
    void usesHandSizeAtResolution() {
        Forest handForest = new Forest();
        GiantGrowth sought = new GiantGrowth();
        GrizzlyBears different = new GrizzlyBears();
        harness.setHand(player1, List.of(new GiantGrowth(), handForest));
        harness.setLibrary(player1, List.of(different, sought));
        Permanent collector = addAttackingCollector();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, collector.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handForest, sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(different);
    }

    @Test
    @DisplayName("Seeks exactly one matching card and preserves the remaining library order")
    void seeksOnlyOneMatchingCard() {
        Forest first = new Forest();
        Forest last = new Forest();
        GiantGrowth match1 = new GiantGrowth();
        GiantGrowth match2 = new GiantGrowth();
        harness.setHand(player1, List.of(new Forest()));
        List<Card> library = List.of(first, match1, match2, last);
        harness.setLibrary(player1, library);
        addAttackingCollector();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        var sought = gd.playerHands.get(player1.getId()).get(1);
        assertThat(sought).isIn(match1, match2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(library.stream().filter(card -> card != sought).toList());
    }

    @Test
    @DisplayName("An empty library does not cause a failed draw")
    void emptyLibraryDoesNothing() {
        Forest handCard = new Forest();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of());
        addAttackingCollector();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Paying ward lets the opponent's spell resolve")
    void paidWardLetsSpellResolve() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new ObsessiveCollector());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, collector.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(collector.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new ObsessiveCollector());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, collector.getId());

        assertThat(collector.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Ward also counters an opponent's activated ability")
    void wardCountersUnpaidActivatedAbility() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new ObsessiveCollector());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, collector.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(collector.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingCollector() {
        Permanent collector = addCreatureReady(player1, new ObsessiveCollector());
        collector.setAttacking(true);
        return collector;
    }
}
