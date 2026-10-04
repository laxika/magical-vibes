package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HostageTaker.class, GoblinPiker.class, LightningBolt.class, RodOfRuin.class, Unsummon.class, Hijack.class})
class HostageTakerTest extends BaseCardTest {

    /**
     * Casts Hostage Taker targeting the given permanent, resolves the creature spell
     * and the ETB trigger that exiles the target.
     */
    private void castAndExileTarget(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HostageTaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Target is chosen at cast time for non-may ETB effects
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> creature enters, ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger -> target is exiled
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("ETB exiles target creature")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("ETB exiles target artifact")
    void etbExilesTargetArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        castAndExileTarget(artifactId);

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    @Test
    @DisplayName("Exiled card returns when Hostage Taker dies")
    void exiledCardReturnsWhenHostageTakerDies() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));

        resetForFollowUpSpell();

        // Kill Hostage Taker with Lightning Bolt
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hostageTakerId);

        // Hostage Taker is dead
        harness.assertNotOnBattlefield(player1, "Hostage Taker");

        // Exiled card returns to battlefield under owner's control
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled card returns when Hostage Taker is bounced")
    void exiledCardReturnsWhenHostageTakerBounced() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        resetForFollowUpSpell();

        // Bounce Hostage Taker with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hostageTakerId);

        // Exiled card returns to battlefield
        harness.assertOnBattlefield(player2, "Goblin Piker");
    }

    @Test
    @DisplayName("Exiled card is tracked with source for cast-from-exile")
    void exiledCardIsTrackedWithSource() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        List<Card> exiledWithHT = gd.getCardsExiledByPermanent(hostageTakerId);
        assertThat(exiledWithHT).hasSize(1);
        assertThat(exiledWithHT.getFirst().getName()).isEqualTo("Goblin Piker");
    }

    @Test
    @DisplayName("Can cast exiled creature with any mana type")
    void canCastExiledCreatureWithAnyMana() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        resetForFollowUpSpell();

        // Add only white mana — Goblin Piker costs {1}{R} but any mana can be spent
        Card exiledPiker = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, exiledPiker.getId());
        harness.passBothPriorities();

        // Goblin Piker should be on the battlefield under player1's control
        harness.assertOnBattlefield(player1, "Goblin Piker");
    }

    @Test
    @DisplayName("Can cast exiled artifact with any mana type")
    void canCastExiledArtifactWithAnyMana() {
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        castAndExileTarget(artifactId);

        resetForFollowUpSpell();

        // Rod of Ruin costs {4} — use any mana
        Card exiledRod = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Rod of Ruin"))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castFromExile(player1, exiledRod.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rod of Ruin");
    }

    @Test
    @DisplayName("Casting exiled card prevents return when Hostage Taker leaves")
    void castingExiledCardPreventsReturn() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        resetForFollowUpSpell();

        // Cast the exiled creature
        Card exiledPiker = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, exiledPiker.getId());
        harness.passBothPriorities();

        // Piker is now on player1's battlefield
        harness.assertOnBattlefield(player1, "Goblin Piker");

        resetForFollowUpSpell();

        // Now kill Hostage Taker
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hostageTakerId);

        // Goblin Piker should NOT be returned — it was already cast from exile
        // Player2 should NOT get a second Goblin Piker
        long pikerCount = countPermanents(player2, "Goblin Piker");
        assertThat(pikerCount).isZero();
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after Hostage Taker leaves")
    void exileTrackingCleanedUp() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Kill Hostage Taker
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hostageTakerId);

        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    private UUID exileCreatureThenStealHostageTaker() {
        harness.addToBattlefield(player2, new GoblinPiker());
        castAndExileTarget(harness.getPermanentId(player2, "Goblin Piker"));
        UUID exiledId = gd.getPlayerExiledCards(player2.getId()).getFirst().getId();
        UUID sourceId = harness.getPermanentId(player1, "Hostage Taker");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Hijack()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, sourceId);
        harness.assertOnBattlefield(player2, "Hostage Taker");
        return exiledId;
    }

    @Test
    @DisplayName("The original trigger controller retains casting permission after Hostage Taker changes control")
    void originalControllerCanStillCastAfterControlChange() {
        UUID exiledId = exileCreatureThenStealHostageTaker();
        resetForFollowUpSpell();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castFromExile(player1, exiledId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Piker");
        harness.assertOnBattlefield(player2, "Hostage Taker");
    }

    @Test
    @DisplayName("Gaining control of Hostage Taker does not grant permission to cast its exiled card")
    void newControllerCannotCastExiledCard() {
        UUID exiledId = exileCreatureThenStealHostageTaker();
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, exiledId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(exiledId));
    }

    @Test
    @DisplayName("The target is not exiled if Hostage Taker leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID targetId = harness.getPermanentId(player2, "Goblin Piker");
        harness.setHand(player1, List.of(new HostageTaker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Hostage Taker"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hostage Taker");
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hostage Taker can exile its controller's own creature")
    void canExileOwnCreature() {
        harness.addToBattlefield(player1, new GoblinPiker());
        castAndExileTarget(harness.getPermanentId(player1, "Goblin Piker"));

        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("Exiled creatures still require normal sorcery timing")
    void castingPermissionDoesNotGrantFlash() {
        harness.addToBattlefield(player2, new GoblinPiker());
        castAndExileTarget(harness.getPermanentId(player2, "Goblin Piker"));
        UUID exiledId = gd.getPlayerExiledCards(player2.getId()).getFirst().getId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(exiledId));
    }
}
