package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twitch.class, GrizzlyBears.class, Forest.class, AngelsFeather.class, Pacifism.class})
class TwitchTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Twitch puts it on the stack targeting a permanent")
    void castingPutsItOnStack() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareTwitch();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Cannot cast Twitch without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twitch()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Tapping untapped permanents =====

    @Test
    @DisplayName("Taps an untapped creature")
    void tapsUntappedCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThat(target.isTapped()).isFalse();

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped land")
    void tapsUntappedLand() {
        Permanent target = addReadyLand(player2);

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps an untapped artifact")
    void tapsUntappedArtifact() {
        Permanent target = addReadyArtifact(player2);

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Untapping tapped permanents =====

    @Test
    @DisplayName("Untaps a tapped creature")
    void untapsTappedCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        assertThat(target.isTapped()).isTrue();

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps a tapped land")
    void untapsTappedLand() {
        Permanent target = addReadyLand(player2);
        target.tap();

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        Permanent target = addReadyArtifact(player2);
        target.tap();

        castAndAcceptTwitch(target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    // ===== Invalid targets =====

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addCreatureReady(player1, new GrizzlyBears()); // valid target so spell is playable
        Permanent enchantment = addReadyEnchantment(player2);
        prepareTwitch();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land");
    }

    // ===== Drawing a card =====

    @Test
    @DisplayName("Draws a card after resolving")
    void drawsACard() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castAndAcceptTwitch(target.getId());

        // Hand should have 1 card (Twitch left hand, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws a card even when untapping")
    void drawsACardWhenUntapping() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castAndAcceptTwitch(target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Offers the tap or untap choice at resolution")
    void offersTapOrUntapChoiceAtResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareTwitch();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can decline tapping or untapping and still draw a card")
    void canDeclineTapOrUntapAndStillDraws() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        prepareTwitch();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== After resolution =====

    @Test
    @DisplayName("Twitch goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndAcceptTwitch(target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Twitch");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        prepareTwitch();

        harness.castInstant(player1, 0, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Can target own permanents =====

    @Test
    @DisplayName("Can tap own untapped creature")
    void canTapOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castAndAcceptTwitch(ownCreature.getId());

        assertThat(ownCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.tap();

        castAndAcceptTwitch(ownCreature.getId());

        assertThat(ownCreature.isTapped()).isFalse();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Tapping logs correct message")
    void tappingLogsMessage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAndAcceptTwitch(target.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Twitch") && log.contains("taps") && log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Untapping logs correct message")
    void untappingLogsMessage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castAndAcceptTwitch(target.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("Twitch") && log.contains("untaps") && log.contains("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AngelsFeather());
    }

    private Permanent addReadyEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Pacifism());
    }

    private void prepareTwitch() {
        harness.setHand(player1, List.of(new Twitch()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private void castAndAcceptTwitch(UUID targetId) {
        prepareTwitch();
        harness.castAndResolveInstant(player1, 0, targetId);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
    }
}

