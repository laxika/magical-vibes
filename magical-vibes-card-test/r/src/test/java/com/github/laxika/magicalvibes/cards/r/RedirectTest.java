package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Redirect.class, Boomerang.class, CounselOfTheSoratami.class, GrizzlyBears.class, LavaAxe.class, ConeOfFlame.class})
class RedirectTest extends BaseCardTest {

    @Test
    @DisplayName("Redirect allows exchanging two distinct targets of the same spell")
    void canExchangeTargets() {
        UUID first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        ConeOfFlame cone = new ConeOfFlame();
        harness.setHand(player1, List.of(cone));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, List.of(first, second, player1.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, cone.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(second);
        harness.handlePermanentChosen(player2, second);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, first);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Casting Redirect puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        Boomerang boomerang = new Boomerang();
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry redirectEntry = gd.stack.getLast();
        assertThat(redirectEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(redirectEntry.getCard().getName()).isEqualTo("Redirect");
        assertThat(redirectEntry.getTargetId()).isEqualTo(boomerang.getId());
    }

    @Test
    @DisplayName("Redirect can target any spell including creature spells")
    void canTargetAnySpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry redirectEntry = gd.stack.getLast();
        assertThat(redirectEntry.getCard().getName()).isEqualTo("Redirect");
    }

    @Test
    @DisplayName("Resolving Redirect on a targeted spell offers may-ability to retarget")
    void resolvingOffersRetargetChoice() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Redirect
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accepting retarget and choosing new target changes the spell's target")
    void acceptRetargetChangesTarget() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        // Player1 casts Boomerang targeting their own bears
        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Redirect
        harness.passBothPriorities();
        // Accept retarget
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose player2's bears as new target
        harness.handlePermanentChosen(player2, bears2PermId);

        // Verify the spell's target changed
        StackEntry boomerangEntry = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Boomerang"))
                .findFirst().orElseThrow();
        assertThat(boomerangEntry.getTargetId()).isEqualTo(bears2PermId);

        // Resolve Boomerang — bounces player2's bears
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Player1's bears should still be on battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining retarget keeps the original target unchanged")
    void decliningKeepsOriginalTarget() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Redirect
        harness.passBothPriorities();
        // Decline retarget
        harness.handleMayAbilityChosen(player2, false);

        // Resolve Boomerang — bounces player1's bears (original target)
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving Redirect on an untargeted spell offers may prompt but accepting has no effect")
    void resolvingOnUntargetedSpellHasNoEffect() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Redirect — may prompt appears
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // Accept — but no valid new targets for untargeted spell
        harness.handleMayAbilityChosen(player2, true);

        // Counsel should still be on the stack, unaffected
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Redirect can retarget a player-targeting spell")
    void canRetargetPlayerTargetSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());

        // Resolve Redirect
        harness.passBothPriorities();
        // Accept retarget
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(player1.getId());

        // Retarget to player1
        harness.handlePermanentChosen(player2, player1.getId());

        // Resolve Lava Axe — hits player1
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Redirect goes to caster's graveyard after resolving")
    void goesToCasterGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Redirect
        harness.passBothPriorities();
        // Decline retarget
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Redirect");
    }

    @Test
    @DisplayName("Redirect fizzles if target spell is removed from the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Remove target spell to simulate it being countered
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Boomerang"));

        harness.passBothPriorities();

        // Redirect fizzles
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Redirect");
    }

    @Test
    @DisplayName("Stack is empty after Redirect and original spell resolve")
    void stackEmptyAfterFullResolution() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Redirect
        harness.passBothPriorities();
        // Accept retarget
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, bears2PermId);

        // Resolve Boomerang
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }
}
