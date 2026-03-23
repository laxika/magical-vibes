package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    // ===== Card properties =====

    @Test
    @DisplayName("Card has ExileTargetPermanentUntilSourceLeavesEffect on ETB")
    void hasCorrectETBEffect() {
        HostageTaker card = new HostageTaker();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ExileTargetPermanentUntilSourceLeavesEffect.class);
    }

    @Test
    @DisplayName("Card has AllowCastFromCardsExiledWithSourceEffect with anyManaType")
    void hasCorrectStaticEffect() {
        HostageTaker card = new HostageTaker();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(AllowCastFromCardsExiledWithSourceEffect.class);
        AllowCastFromCardsExiledWithSourceEffect effect =
                (AllowCastFromCardsExiledWithSourceEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.anyManaType()).isTrue();
    }

    @Test
    @DisplayName("ETB is mandatory (not a may ability)")
    void etbIsMandatory() {
        HostageTaker card = new HostageTaker();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isNotInstanceOf(com.github.laxika.magicalvibes.model.effect.MayEffect.class);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB exiles target creature")
    void etbExilesTargetCreature() {
        harness.addToBattlefield(player2, new GoblinPiker());
        UUID creatureId = harness.getPermanentId(player2, "Goblin Piker");
        castAndExileTarget(creatureId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Goblin Piker"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goblin Piker"));
    }

    @Test
    @DisplayName("ETB exiles target artifact")
    void etbExilesTargetArtifact() {
        harness.addToBattlefield(player2, new RodOfRuin());
        UUID artifactId = harness.getPermanentId(player2, "Rod of Ruin");
        castAndExileTarget(artifactId);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));
    }

    // ===== LTB return =====

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
        harness.castInstant(player2, 0, hostageTakerId);
        harness.passBothPriorities();

        // Hostage Taker is dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hostage Taker"));

        // Exiled card returns to battlefield under owner's control
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
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
        harness.castInstant(player2, 0, hostageTakerId);
        harness.passBothPriorities();

        // Exiled card returns to battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
    }

    // ===== Cast from exile =====

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
        gs.playCardFromExile(gd, player1, exiledPiker.getId(), null, null);
        harness.passBothPriorities();

        // Goblin Piker should be on the battlefield under player1's control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));
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
        gs.playCardFromExile(gd, player1, exiledRod.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
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
        gs.playCardFromExile(gd, player1, exiledPiker.getId(), null, null);
        harness.passBothPriorities();

        // Piker is now on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Piker"));

        resetForFollowUpSpell();

        // Now kill Hostage Taker
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID hostageTakerId = harness.getPermanentId(player1, "Hostage Taker");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, hostageTakerId);
        harness.passBothPriorities();

        // Goblin Piker should NOT be returned — it was already cast from exile
        // Player2 should NOT get a second Goblin Piker
        long pikerCount = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .count();
        assertThat(pikerCount).isZero();
    }

    // ===== Edge cases =====

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
        harness.castInstant(player2, 0, hostageTakerId);
        harness.passBothPriorities();

        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }
}
