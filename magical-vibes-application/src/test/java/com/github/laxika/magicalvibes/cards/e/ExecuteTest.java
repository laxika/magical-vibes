package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;

@CardUsed({Execute.class, AvenFlock.class, AirElemental.class, CircleOfProtectionBlack.class, GlorySeeker.class})
class ExecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Execute destroys a white creature and draws a card")
    void resolvingDestroysWhiteCreatureAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());

        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Execute can target a white creature you control")
    void canTargetWhiteCreatureYouControl() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new AvenFlock());

        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, hawk.getId());

        harness.assertNotOnBattlefield(player1, "Aven Flock");
        harness.assertInGraveyard(player1, "Aven Flock");
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Execute destroys the creature even with a regeneration shield")
    void cannotBeRegenerated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        target.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Cannot target a non-white creature")
    void cannotTargetNonWhiteCreature() {
        // A legal white target elsewhere keeps Execute playable, so the rejection is the filter message.
        harness.addToBattlefield(player1, new GlorySeeker());

        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNoncreaturePermanent() {
        // A legal white creature elsewhere keeps Execute playable, so the rejection is the creature filter.
        harness.addToBattlefield(player1, new AvenFlock());

        Permanent circle = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());

        harness.setHand(player1, List.of(new Execute()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, circle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("white creature");
        harness.assertOnBattlefield(player2, "Circle of Protection: Black");
    }

    @Test
    @DisplayName("Execute fizzles without drawing if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new AvenFlock());

        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, hawk.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Execute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Execute still draws when an indestructible white creature survives")
    void drawsEvenWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        target.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Glory Seeker");
        harness.assertInHand(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Execute");
    }

    @Test
    @DisplayName("Execute can destroy a multicolored creature that is white")
    void destroysMulticoloredWhiteCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        target.getGrantedColors().add(CardColor.BLUE);
        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Glory Seeker");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInHand(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Execute does not draw when its target gains protection from black")
    void fizzlesIfTargetGainsProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        harness.setHand(player1, List.of(new Execute()));
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        target.getProtectionFromColorsUntilEndOfTurn().add(CardColor.BLACK);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glory Seeker");
        harness.assertInGraveyard(player1, "Execute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
