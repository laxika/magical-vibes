package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({HieromancersCage.class, GreenwoodSentinel.class, Naturalize.class, Forest.class,
        Disperse.class, Manalith.class})
class HieromancersCageTest extends BaseCardTest {

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HieromancersCage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> exile
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesOpponentPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Greenwood Sentinel"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled card returns when Hieromancer's Cage is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID cageId = harness.getPermanentId(player1, "Hieromancer's Cage");
        harness.castAndResolveInstant(player2, 0, cageId);

        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Greenwood Sentinel"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID forestId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        UUID ownPermanentId = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel()).getId();
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownPermanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opposing artifact can be exiled")
    void exilesNoncreaturePermanent() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Manalith()).getId();
        castAndResolve(artifactId);

        harness.assertNotOnBattlefield(player2, "Manalith");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Manalith"));
    }

    @Test
    @DisplayName("Target is not exiled if Cage leaves before its enter trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        UUID cageId = harness.getPermanentId(player1, "Hieromancer's Cage");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, cageId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hieromancer's Cage");
        assertThat(harness.getPermanentId(player2, "Greenwood Sentinel")).isEqualTo(targetId);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bouncing Cage returns its exiled permanent immediately")
    void exiledCardReturnsWhenSourceBounced() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        castAndResolve(targetId);

        UUID cageId = harness.getPermanentId(player1, "Hieromancer's Cage");
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, cageId);

        harness.assertInHand(player1, "Hieromancer's Cage");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(harness.getPermanentId(player2, "Greenwood Sentinel")).isNotEqualTo(targetId);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Enter trigger does nothing if its target leaves before resolution")
    void targetLeavesBeforeTriggerResolves() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Hieromancer's Cage");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cage can enter when the opponent controls no legal target")
    void entersWithoutLegalTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new HieromancersCage(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hieromancer's Cage");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
