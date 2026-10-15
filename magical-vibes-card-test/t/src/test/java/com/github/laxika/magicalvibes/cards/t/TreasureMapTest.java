package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TreasureMap.class})
class TreasureMapTest extends BaseCardTest {

    @Test
    @DisplayName("An empty library does not prevent adding a landmark counter")
    void emptyLibraryStillAddsCounter() {
        harness.setLibrary(player1, List.of());
        Permanent map = addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability taps Treasure Map")
    void activatingTapsTreasureMap() {
        Permanent map = addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(map.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability enters scry state")
    void resolvingEntersScryState() {
        addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Completing scry puts a landmark counter on Treasure Map")
    void scryPutsLandmarkCounter() {
        Permanent map = addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Complete the scry (keep card on top)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
        assertThat(map.isTransformed()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Second activation adds a second landmark counter")
    void secondActivationAddsSecondCounter() {
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Complete scry
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(2);
        assertThat(map.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Third landmark counter triggers transform into Treasure Cove and creates 3 Treasure tokens")
    void transformsAtThreeCounters() {
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 2); // One more needed
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Complete scry
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        // Should have transformed
        assertThat(map.isTransformed()).isTrue();
        assertThat(map.getCard().getName()).isEqualTo("Treasure Cove");
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(0);

        // Should have created 3 Treasure tokens
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long treasureCount = battlefield.stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
        assertThat(treasureCount).isEqualTo(3);
    }

    @Test
    @DisplayName("Transform does not occur below threshold")
    void noTransformBelowThreshold() {
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(map.isTransformed()).isFalse();
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Treasure Cove taps for colorless mana")
    void treasureCoveTapsForColorless() {
        Permanent cove = addTransformedTreasureCove(player1);

        int idx = indexOf(player1, cove);
        harness.activateAbility(player1, idx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Treasure Cove draws a card when sacrificing a Treasure")
    void treasureCoveDrawsCardOnTreasureSacrifice() {
        Permanent cove = addTransformedTreasureCove(player1);
        addTreasureToken(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        int coveIdx = indexOf(player1, cove);
        // Activate second ability (index 1) — sacrifice a Treasure to draw
        harness.activateAbility(player1, coveIdx, 1, null, null);

        // The sacrifice cost handler auto-selects the only valid Treasure
        // Ability goes on stack, resolve it
        harness.passBothPriorities();

        int handSizeAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(handSizeAfter).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Treasure Cove cannot draw if no Treasure to sacrifice")
    void treasureCoveCannotDrawWithoutTreasure() {
        Permanent cove = addTransformedTreasureCove(player1);

        int coveIdx = indexOf(player1, cove);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> harness.activateAbility(player1, coveIdx, 1, null, null));
    }

    @Test
    @DisplayName("A departed Map with three landmark counters still creates Treasures")
    void departedMapUsesLastKnownLandmarkCounters() {
        harness.setLibrary(player1, List.of());
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, map));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
        harness.assertInGraveyard(player1, "Treasure Map");
    }

    @Test
    @DisplayName("A departed Map below three landmark counters creates no Treasures")
    void departedMapBelowThresholdCreatesNoTreasures() {
        harness.setLibrary(player1, List.of());
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, map));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("A Map already transformed during resolution is not transformed again")
    void priorTransformationPreventsSecondTransformation() {
        harness.setLibrary(player1, List.of());
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        // Model another effect transforming the source while its ability is on the stack.
        map.setCard(map.getCard().getBackFaceCard());
        map.setTransformed(true);
        harness.passBothPriorities();

        assertThat(map.isTransformed()).isTrue();
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    @DisplayName("Transformation removes all landmark counters but retains other counters and tapped status")
    void excessLandmarkCountersAreRemoved() {
        harness.setLibrary(player1, List.of());
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 4);
        map.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(map.isTransformed()).isTrue();
        assertThat(map.isTapped()).isTrue();
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isZero();
        assertThat(map.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    @DisplayName("Putting the scried card on the bottom continues the ability")
    void bottomingScriedCardStillAddsCounter() {
        TreasureMap top = new TreasureMap();
        TreasureMap next = new TreasureMap();
        harness.setLibrary(player1, List.of(top, next));
        Permanent map = addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Treasure Cove taps and sacrifices a Treasure before its draw resolves")
    void treasureCovePaysCostsBeforeDrawing() {
        Permanent cove = addTransformedTreasureCove(player1);
        Permanent treasure = addTreasureToken(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, cove), 1, null, null);

        assertThat(cove.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(treasure);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    private Permanent addReadyTreasureMap(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TreasureMap());
    }

    private Permanent addTransformedTreasureCove(Player player) {
        Permanent perm = addReadyTreasureMap(player);
        perm.setCard(perm.getCard().getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Permanent addTreasureToken(Player player) {
        Permanent map = addReadyTreasureMap(player);
        map.setCounterCount(CounterType.LANDMARK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.activateAbility(player, indexOf(player, map), null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        List<Permanent> treasures = findPermanents(player, "Treasure");
        // Keep a single Treasure so the cost has exactly one legal choice.
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToExile(gd, treasures.get(1));
            harness.getPermanentRemovalService().removePermanentToExile(gd, treasures.get(2));
        });
        return treasures.getFirst();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
