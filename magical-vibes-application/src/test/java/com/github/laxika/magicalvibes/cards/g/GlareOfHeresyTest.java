package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FleecemaneLion;
import com.github.laxika.magicalvibes.cards.h.HeliodGodOfTheSun;
import com.github.laxika.magicalvibes.cards.s.SedgeScorpion;
import com.github.laxika.magicalvibes.cards.s.SpearOfHeliod;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
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

@CardUsed({GlareOfHeresy.class, YokedOx.class, SedgeScorpion.class, SpearOfHeliod.class,
        FleecemaneLion.class, HeliodGodOfTheSun.class})
class GlareOfHeresyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target white permanent")
    void exilesTargetWhitePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YokedOx());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Yoked Ox");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Yoked Ox"));
        harness.assertNotInGraveyard(player2, "Yoked Ox");
    }

    @Test
    @DisplayName("Cannot target a nonwhite permanent")
    void cannotTargetNonwhitePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SedgeScorpion());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile a white permanent you control")
    void exilesOwnWhitePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YokedOx());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Yoked Ox");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Yoked Ox"));
        harness.assertNotInGraveyard(player1, "Yoked Ox");
    }

    @Test
    @DisplayName("Can exile a white noncreature permanent")
    void exilesWhiteNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpearOfHeliod());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Spear of Heliod");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Spear of Heliod"));
        harness.assertNotInGraveyard(player2, "Spear of Heliod");
    }

    @Test
    @DisplayName("Can exile a multicolored permanent that is white")
    void exilesMulticoloredWhitePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FleecemaneLion());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Fleecemane Lion");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Fleecemane Lion"));
        harness.assertNotInGraveyard(player2, "Fleecemane Lion");
    }

    @Test
    @DisplayName("Exile ignores indestructible even when a God is not a creature")
    void exilesIndestructibleWhitePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HeliodGodOfTheSun());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Heliod, God of the Sun");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Heliod, God of the Sun"));
        harness.assertNotInGraveyard(player2, "Heliod, God of the Sun");
    }

    @Test
    @DisplayName("Does not exile a target that gains hexproof in response")
    void targetGainingHexproofIsIllegalOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FleecemaneLion());
        harness.setHand(player1, List.of(new GlareOfHeresy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fleecemane Lion");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Fleecemane Lion"));
        harness.assertInGraveyard(player1, "Glare of Heresy");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
