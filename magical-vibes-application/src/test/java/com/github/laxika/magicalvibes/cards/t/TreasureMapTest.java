package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureMapTest extends BaseCardTest {

    // ===== Card structure =====

    

    @Test
    @DisplayName("Has back face configured as Treasure Cove")
    void hasBackFace() {
        TreasureMap card = new TreasureMap();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceClassName()).isEqualTo("TreasureCove");
    }

    

    // ===== Activating ability =====

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

    // ===== Scry resolution =====

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

    // ===== Landmark counters =====

    @Test
    @DisplayName("Completing scry puts a landmark counter on Treasure Map")
    void scryPutsLandmarkCounter() {
        Permanent map = addReadyTreasureMap(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Complete the scry (keep card on top)
        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
        assertThat(map.isTransformed()).isFalse();
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
        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(2);
        assertThat(map.isTransformed()).isFalse();
    }

    // ===== Transform at 3 counters =====

    @Test
    @DisplayName("Third landmark counter triggers transform into Treasure Cove and creates 3 Treasure tokens")
    void transformsAtThreeCounters() {
        Permanent map = addReadyTreasureMap(player1);
        map.setCounterCount(CounterType.LANDMARK, 2); // One more needed
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Complete scry
        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

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

        gs.handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(map.isTransformed()).isFalse();
        assertThat(map.getCounterCount(CounterType.LANDMARK)).isEqualTo(1);
    }

    // ===== Back face: Treasure Cove =====

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
        Permanent treasure = addTreasureToken(player1);

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

    // ===== Helpers =====

    private Permanent addReadyTreasureMap(Player player) {
        TreasureMap card = new TreasureMap();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedTreasureCove(Player player) {
        TreasureMap card = new TreasureMap();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTreasureToken(Player player) {
        Card tokenCard = createTreasureToken();
        Permanent perm = new Permanent(tokenCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        return card;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
