package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GreaterGood;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Slay.class, GrizzlyBears.class, AirElemental.class, GloriousAnthem.class, GreaterGood.class})
class SlayTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Slay destroys a green creature and draws a card")
    void resolvingDestroysGreenCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Slay was cast from a one-card hand, so the only card in hand is the one drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Slay can destroy a green creature controlled by its caster")
    void canTargetControllerGreenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Slay can target a green creature you control")
    void canTargetOwnGreenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Slay destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-green creature")
    void cannotTargetNonGreenCreature() {
        // A legal green target elsewhere keeps Slay playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        // A legal green target elsewhere keeps Slay playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Slay does not draw when its only target leaves before resolution")
    void fizzlesWithoutDrawingIfTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Slay");
    }

    @Test
    @DisplayName("Slay cannot target a green noncreature permanent")
    void cannotTargetGreenNonCreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GreaterGood());
        harness.setHand(player1, List.of(new Slay()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @DisplayName("Slay draws even when an indestructible green creature survives")
    void drawsWhenIndestructibleTargetSurvives() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Air Elemental");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Slay");
    }

    @Test
    @DisplayName("Slay does not draw if its target gains shroud before resolution")
    void doesNotDrawWhenTargetGainsShroud() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Slay()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        bears.getPersistentGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Slay");
    }
}
