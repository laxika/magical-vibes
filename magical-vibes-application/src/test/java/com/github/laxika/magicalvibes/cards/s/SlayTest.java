package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.c.CloudCover;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Slay.class, AlphaKavu.class, ArcticMerfolk.class, CloudCover.class})
class SlayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Slay destroys a green creature and draws a card")
    void resolvingDestroysGreenCreatureAndDraws() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, kavu.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Alpha Kavu");
        harness.assertInGraveyard(player2, "Alpha Kavu");
        // Slay was cast from a one-card hand, so the only card in hand is the one drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Arctic Merfolk");
    }

    @Test
    @DisplayName("Slay can target a green creature you control")
    void canTargetOwnGreenCreature() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new AlphaKavu());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, kavu.getId());

        harness.assertNotOnBattlefield(player1, "Alpha Kavu");
        harness.assertInGraveyard(player1, "Alpha Kavu");
        harness.assertInHand(player1, "Arctic Merfolk");
    }

    @Test
    @DisplayName("Slay destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());
        kavu.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, kavu.getId());

        harness.assertNotOnBattlefield(player2, "Alpha Kavu");
        harness.assertInGraveyard(player2, "Alpha Kavu");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        // A legal green target elsewhere keeps Slay playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new AlphaKavu());

        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new ArcticMerfolk());

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        // A legal green target elsewhere keeps Slay playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new AlphaKavu());

        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CloudCover());

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Slay does not draw when its only target leaves before resolution")
    void fizzlesWithoutDrawingIfTargetLeavesBeforeResolution() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new ArcticMerfolk()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, kavu.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Slay");
    }
}
