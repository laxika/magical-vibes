package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildRicochetTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Wild Ricochet puts it on the stack targeting an instant or sorcery spell")
    void castingPutsOnStackTargetingSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry ricochetEntry = gd.stack.getLast();
        assertThat(ricochetEntry.getCard().getName()).isEqualTo("Wild Ricochet");
        assertThat(ricochetEntry.getTargetId()).isEqualTo(lavaAxe.getId());
    }

    @Test
    @DisplayName("Cannot target a creature spell with Wild Ricochet")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving offers the may-ability to choose new targets for the original spell")
    void resolvingOffersRetargetOriginalPrompt() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Wild Ricochet
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the original retarget still copies the spell, and the copy inherits the original target")
    void decliningOriginalRetargetStillCopies() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        // Lava Axe targets player2 (the Wild Ricochet caster)
        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Wild Ricochet → may prompt to retarget original
        harness.passBothPriorities();
        // Decline retargeting the original
        harness.handleMayAbilityChosen(player2, false);

        GameData gd = harness.getGameData();
        // A copy of Lava Axe was created; a copy-retarget may prompt is now offered
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Lava Axe");
        assertThat(copyEntry.getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Retargeting the original redirects both the original and its copy")
    void retargetOriginalRedirectsBoth() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Lava Axe originally targets player2 (the Wild Ricochet caster)
        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Wild Ricochet → accept retargeting the original to player1
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());

        // The copy is now created inheriting the retargeted (player1) target; decline retargeting it
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // Resolve copy, then original — both hit player1
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Copy can be given a different target than the original")
    void copyCanBeRetargetedSeparately() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Lava Axe originally targets player1
        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Wild Ricochet → decline original retarget (original keeps player1)
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        // Accept retargeting the copy to player2
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player2.getId());

        // Resolve copy (hits player2), then original (hits player1)
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore - 5);
    }

    // ===== Copy does not persist =====

    @Test
    @DisplayName("Wild Ricochet goes to its caster's graveyard and the copy does not go to any graveyard")
    void ricochetToGraveyardCopyDoesNot() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new WildRicochet()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Wild Ricochet → decline both retargets
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        // Resolve copy, then original
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Wild Ricochet"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lava Axe"));
        // The original Lava Axe belongs to player1's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lava Axe"));
        assertThat(gd.stack).isEmpty();
    }
}
