package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CastFromZoneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncreasingVengeanceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Increasing Vengeance copies the spell, with a graveyard-conditional second copy and flashback {3}{R}{R}")
    void hasCorrectProperties() {
        IncreasingVengeance card = new IncreasingVengeance();

        assertThat(EffectResolution.needsSpellTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        // Base copy.
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CopySpellEffect.class);

        // Extra copy only when cast from a graveyard.
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(CastFromZoneConditionalEffect.class);
        CastFromZoneConditionalEffect conditional =
                (CastFromZoneConditionalEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(conditional.sourceZone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(conditional.wrapped()).isInstanceOf(CopySpellEffect.class);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{3}{R}{R}");
    }

    // ===== Targeting =====

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

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry vengeance = gd.stack.getLast();
        assertThat(vengeance.getCard().getName()).isEqualTo("Increasing Vengeance");
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

    // ===== Normal cast: one copy =====

    @Test
    @DisplayName("Cast from hand creates a single copy of the target spell")
    void normalCastCreatesOneCopy() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel, new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.castInstant(player1, 0, counsel.getId());

        // Resolve Increasing Vengeance -> one copy created
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Original Counsel + one copy
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    // ===== Flashback cast: two copies =====

    @Test
    @DisplayName("Cast from graveyard via flashback creates two copies of the target spell")
    void flashbackCastCreatesTwoCopies() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 5); // pays {3}{R}{R}

        harness.castSorcery(player1, 0, 0);
        harness.castFlashback(player1, 0, counsel.getId());

        // Resolve Increasing Vengeance -> two copies created
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Original Counsel + two copies
        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(e -> e.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Flashback copies a targeted spell twice, each copy keeping the same target")
    void flashbackCopiesTargetedSpellTwice() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.setGraveyard(player1, List.of(new IncreasingVengeance()));
        harness.addMana(player1, ManaColor.BLUE, 2); // Boomerang {U}{U}
        harness.addMana(player1, ManaColor.RED, 5);   // flashback {3}{R}{R}

        harness.castInstant(player1, 0, bearsPermId);
        harness.castFlashback(player1, 0, boomerang.getId());

        // Resolve Increasing Vengeance -> first copy created, retarget prompt for copy 1
        harness.passBothPriorities();
        // Decline retarget for the first copy -> resolution resumes and creates the second copy
        harness.handleMayAbilityChosen(player1, false);
        // Decline retarget for the second copy
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
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
        harness.castFlashback(player1, 0, counsel.getId());

        // Resolve Increasing Vengeance
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Increasing Vengeance"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Vengeance"));
    }
}
