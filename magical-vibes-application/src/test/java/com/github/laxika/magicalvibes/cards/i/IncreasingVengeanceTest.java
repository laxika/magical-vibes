package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Fling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.t.TwinningStaff;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncreasingVengeance.class, CounselOfTheSoratami.class, Boomerang.class, GrizzlyBears.class,
        TwinningStaff.class, Fling.class})
class IncreasingVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Can target an instant or sorcery spell you control")
    void canTargetOwnSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        // player1 still has priority and casts Increasing Vengeance targeting its own spell
        harness.castInstant(player1, 0, counsel.getId());

        assertThat(gd.stack).hasSize(2);
        StackEntry vengeance = gd.stack.getLast();
        assertThat(vengeance.getTargetId()).isEqualTo(counsel.getId());
    }

    @Test
    @DisplayName("Cannot target a spell controlled by another player")
    void cannotTargetOpponentSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new IncreasingVengeance()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        UUID counselCardId = counsel.getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, counselCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast from hand creates a single copy of the target spell")
    void normalCastCreatesOneCopy() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, counsel.getId());

        // Original Counsel + one copy
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Cast from graveyard via flashback creates two copies of the target spell")
    void flashbackCastCreatesTwoCopies() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 5); // pays {3}{R}{R}

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveFlashback(player1, 0, counsel.getId());

        // Original Counsel + two copies
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(e -> e.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Flashback copies a targeted spell twice, each copy keeping the same target")
    void flashbackCopiesTargetedSpellTwice() {
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 2); // Boomerang {U}{U}
        harness.addMana(player1, ManaColor.RED, 5);   // flashback {3}{R}{R}

        harness.castInstant(player1, 0, bearsPermId);
        harness.castAndResolveFlashback(player1, 0, boomerang.getId());
        // Decline retarget for the first copy -> resolution resumes and creates the second copy
        harness.handleMayAbilityChosen(player1, false);
        // Decline retarget for the second copy
        harness.handleMayAbilityChosen(player1, false);

        // Original Boomerang + two copies, all targeting the same Grizzly Bears
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(e -> e.getControllerId().equals(player1.getId()))
                .allMatch(e -> bearsPermId.equals(e.getTargetId()))
                .allMatch(e -> e.getDescription().equals("Copy of Boomerang"));
    }

    @Test
    @DisplayName("Flashback exiles Increasing Vengeance after resolving")
    void flashbackExilesAfterResolving() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveFlashback(player1, 0, counsel.getId());

        harness.assertNotInGraveyard(player1, "Increasing Vengeance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Vengeance"));
    }

    @Test
    @DisplayName("Each flashback copy can choose its own new target")
    void copiesCanChooseIndependentTargets() {
        UUID first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, first);
        harness.castAndResolveFlashback(player1, 0, boomerang.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .extracting(StackEntry::getTargetId).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(first);
    }

    @Test
    @DisplayName("Twinning Staff adds only one copy to the two created by flashback")
    void flashbackWithTwinningStaffCreatesThreeCopies() {
        harness.addToBattlefield(player1, new TwinningStaff());
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveFlashback(player1, 0, counsel.getId());

        assertThat(gd.stack).hasSize(4);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);
    }

    @Test
    @DisplayName("A copy of graveyard-cast Increasing Vengeance creates only one copy")
    void copiedFlashbackSpellWasNotCastFromGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        IncreasingVengeance flashback = new IncreasingVengeance();
        harness.setHand(player1, List.of(counsel, new IncreasingVengeance()));
        harness.setGraveyard(player1, List.of(flashback));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.castFlashback(player1, 0, counsel.getId());
        harness.castAndResolveInstant(player1, 0, flashback.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isNull();
    }

    @Test
    @DisplayName("Cannot copy a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies preserve Fling's sacrificed power without requiring another sacrifice")
    void copiesPreserveAdditionalCostInformation() {
        UUID sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        Fling fling = new Fling();
        harness.setHand(player1, List.of(fling));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), sacrifice);
        harness.castAndResolveFlashback(player1, 0, fling.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
