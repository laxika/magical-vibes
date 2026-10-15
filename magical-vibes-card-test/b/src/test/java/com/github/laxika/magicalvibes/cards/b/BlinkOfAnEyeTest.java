package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlinkOfAnEye.class, AngelicChorus.class, GrizzlyBears.class, Island.class, Spellbook.class})
class BlinkOfAnEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker — bounces target creature, does not draw a card")
    void bouncesCreatureWithoutKicker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1); // 1 generic

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        // No card drawn (hand size stays the same minus the spell cast)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore - 1);
    }

    @Test
    @DisplayName("With kicker — bounces target creature and draws a card")
    void bouncesCreatureWithKicker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // {1}{U} base + {1}{U} kicker

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        // Drew a card (hand size = before - 1 spell cast + 1 draw = same as before)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Bounces target artifact")
    void bouncesArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("Bounces target enchantment")
    void bouncesEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AngelicChorus()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Can bounce own permanent")
    void canBounceOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Blink of an Eye");
    }

    @Test
    @DisplayName("Kicked spell does not draw when its only target leaves the battlefield")
    void kickedSpellDoesNotDrawWithMissingTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        Island draw = new Island();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertInGraveyard(player1, "Blink of an Eye");
    }

    @Test
    @DisplayName("Returns a permanent to its owner and the caster draws when kicked")
    void returnsToOwnerRatherThanController() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, bears).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.setHand(player2, List.of());
        Island draw = new Island();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, draw);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kicker requires its additional mana cost")
    void cannotKickWithOnlyBaseMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Blink of an Eye");
        assertThat(gd.stack).isEmpty();
    }
}
