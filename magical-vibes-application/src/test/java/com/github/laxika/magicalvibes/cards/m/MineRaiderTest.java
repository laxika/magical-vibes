package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExplosiveDerailment;
import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineRaider.class, WilyGoblin.class, ExplosiveDerailment.class, SterlingHound.class})
class MineRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when you control another outlaw")
    void createsTreasureWithAnotherOutlaw() {
        harness.addToBattlefield(player1, new WilyGoblin());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count itself as another outlaw")
    void doesNotCreateTreasureWithoutAnotherOutlaw() {
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("An opponent's outlaw does not satisfy the condition")
    void opponentOutlawDoesNotCount() {
        harness.addToBattlefield(player2, new WilyGoblin());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Another creature without an outlaw subtype does not satisfy the condition")
    void nonOutlawDoesNotCount() {
        harness.addToBattlefield(player1, new SterlingHound());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("A second Mine Raider counts as another outlaw")
    void anotherMineRaiderCounts() {
        harness.addToBattlefield(player1, new MineRaider());
        castMineRaider();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("No ability triggers if the condition is false on entry")
    void gainingOutlawAfterEntryDoesNotCreateTrigger() {
        castMineRaiderToBattlefield();

        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new MineRaider());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("The condition is checked again when the ability resolves")
    void losingOtherOutlawBeforeResolutionPreventsTreasure() {
        var otherOutlaw = harness.addToBattlefieldAndReturn(player1, new MineRaider());
        castMineRaiderToBattlefield();
        assertThat(gd.stack).hasSize(1);

        destroyCreatureInResponse(otherOutlaw.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mine Raider")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Removing Mine Raider does not stop its trigger if another outlaw remains")
    void removingSourceDoesNotPreventTreasure() {
        var otherOutlaw = harness.addToBattlefieldAndReturn(player1, new MineRaider());
        castMineRaiderToBattlefield();
        var source = findPermanents(player1, "Mine Raider").stream()
                .filter(p -> !p.getId().equals(otherOutlaw.getId())).findFirst().orElseThrow();

        destroyCreatureInResponse(source.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mine Raider")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Trample deals excess damage to the defending player")
    void tramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MineRaider());
        var blocker = addCreatureReady(player2, new SterlingHound());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Mine Raider");
        harness.assertInGraveyard(player2, "Sterling Hound");
    }

    private void destroyCreatureInResponse(UUID targetId) {
        harness.setHand(player2, List.of(new ExplosiveDerailment()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(targetId));
        harness.passBothPriorities();
    }

    private void castMineRaider() {
        castMineRaiderToBattlefield();
        resolveAllTriggers();
    }

    private void castMineRaiderToBattlefield() {
        harness.setHand(player1, List.of(new MineRaider()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
