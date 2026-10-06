package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.w.WalkThePlank;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShelteringLight.class, RaptorCompanion.class,
        Plains.class, LightningStrike.class, WalkThePlank.class})
class ShelteringLightTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sheltering Light puts it on the stack targeting a creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sheltering Light");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving grants indestructible to target creature and enters scry state")
    void resolvingGrantsIndestructibleAndScries() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    // ===== Scry 1 functionality =====

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castAndResolveInstant(player1, 0, targetId);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it to bottom")
    void scryPutOnBottom() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castAndResolveInstant(player1, 0, targetId);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(0)).isNotSameAs(originalTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castAndResolveInstant(player1, 0, targetId);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Sheltering Light fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Raptor Companion");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Sheltering Light");
    }

    @Test
    @DisplayName("Cannot target a noncreature land")
    void cannotTargetNoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting an opposing creature still scries the caster's library")
    void opposingCreatureDoesNotChangeWhoScries() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Card casterTop = new Plains();
        Card opponentTop = new ShelteringLight();
        harness.setLibrary(player1, List.of(casterTop));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(casterTop);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(casterTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        harness.assertInGraveyard(player1, "Sheltering Light");
    }

    @Test
    @DisplayName("An empty library does not stop the creature gaining indestructible")
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ShelteringLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sheltering Light");
    }

    @Test
    @DisplayName("Granted indestructible prevents destruction")
    void survivesDestroySpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new WalkThePlank(), new ShelteringLight()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertNotInGraveyard(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Walk the Plank");
    }

    @Test
    @DisplayName("Granted indestructible prevents lethal damage from destroying the creature")
    void survivesLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new LightningStrike(), new ShelteringLight()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raptor Companion");
        harness.assertNotInGraveyard(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Lightning Strike");
    }
}
