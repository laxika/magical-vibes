package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MyrCustodian;
import com.github.laxika.magicalvibes.cards.t.TestamentBearer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnointWithAffliction.class, Forest.class, MyrCustodian.class, TestamentBearer.class})
class AnointWithAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature with mana value 3 or less")
    void exilesLowManaValueCreature() {
        harness.addToBattlefield(player2, new MyrCustodian());
        castOn(harness.getPermanentId(player2, "Myr Custodian"));

        harness.assertNotOnBattlefield(player2, "Myr Custodian");
        harness.assertNotInGraveyard(player2, "Myr Custodian");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Myr Custodian"));
    }

    @Test
    @DisplayName("May target a creature with mana value greater than 3 when it is not corrupted")
    void highManaValueCreatureIsLegalTargetWithoutCorrupted() {
        harness.addToBattlefield(player2, new TestamentBearer());
        castOn(harness.getPermanentId(player2, "Testament Bearer"));

        harness.assertOnBattlefield(player2, "Testament Bearer");
        harness.assertInGraveyard(player1, "Anoint with Affliction");
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Testament Bearer"));
    }

    @Test
    @DisplayName("Corrupted exiles a creature with mana value greater than 3")
    void corruptedExilesHighManaValueCreature() {
        harness.addToBattlefield(player2, new TestamentBearer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        castOn(harness.getPermanentId(player2, "Testament Bearer"));

        harness.assertNotOnBattlefield(player2, "Testament Bearer");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Testament Bearer"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Two poison counters do not enable corrupted")
    void twoPoisonCountersDoNotExileHighManaValueCreature() {
        harness.addToBattlefield(player2, new TestamentBearer());
        gd.playerPoisonCounters.put(player2.getId(), 2);

        castOn(harness.getPermanentId(player2, "Testament Bearer"));

        harness.assertOnBattlefield(player2, "Testament Bearer");
        harness.assertInGraveyard(player1, "Anoint with Affliction");
    }

    @Test
    @DisplayName("The caster's poison counters do not corrupt an opponent's creature")
    void casterPoisonDoesNotEnableCorruptedForOpponent() {
        harness.addToBattlefield(player2, new TestamentBearer());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        castOn(harness.getPermanentId(player2, "Testament Bearer"));

        harness.assertOnBattlefield(player2, "Testament Bearer");
    }

    @Test
    @DisplayName("Can exile the caster's own high mana value creature when its controller is corrupted")
    void corruptedCanExileOwnCreature() {
        harness.addToBattlefield(player1, new TestamentBearer());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        castOn(harness.getPermanentId(player1, "Testament Bearer"));

        harness.assertNotOnBattlefield(player1, "Testament Bearer");
        harness.assertNotInGraveyard(player1, "Testament Bearer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Testament Bearer"));
    }

    @Test
    @DisplayName("Corrupted is checked at resolution, even if the controller had fewer counters when cast")
    void poisonThresholdReachedBeforeResolutionExilesCreature() {
        harness.addToBattlefield(player2, new TestamentBearer());
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Testament Bearer"));

        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Testament Bearer");
        harness.assertNotInGraveyard(player2, "Testament Bearer");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Testament Bearer"));
    }

    @Test
    @DisplayName("Losing corrupted before resolution prevents exiling a high mana value creature")
    void poisonThresholdLostBeforeResolutionLeavesCreature() {
        harness.addToBattlefield(player2, new TestamentBearer());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Testament Bearer"));

        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Testament Bearer");
        harness.assertInGraveyard(player1, "Anoint with Affliction");
    }

    @Test
    @DisplayName("Corrupted still exiles a low mana value creature")
    void corruptedStillExilesLowManaValueCreature() {
        harness.addToBattlefield(player2, new MyrCustodian());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        castOn(harness.getPermanentId(player2, "Myr Custodian"));

        harness.assertNotOnBattlefield(player2, "Myr Custodian");
        harness.assertNotInGraveyard(player2, "Myr Custodian");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Myr Custodian"));
    }

    private void castOn(UUID targetId) {
        harness.setHand(player1, List.of(new AnointWithAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
