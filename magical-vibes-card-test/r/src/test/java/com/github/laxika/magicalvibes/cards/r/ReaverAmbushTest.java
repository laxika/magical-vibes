package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FrilledDeathspitter;
import com.github.laxika.magicalvibes.cards.m.MomentOfTriumph;
import com.github.laxika.magicalvibes.cards.s.SunSentinel;
import com.github.laxika.magicalvibes.cards.s.StampedingHorncrest;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaverAmbush.class, SunSentinel.class, StampedingHorncrest.class,
        FrilledDeathspitter.class, StriderHarness.class, MomentOfTriumph.class})
class ReaverAmbushTest extends BaseCardTest {

    private void giveReaverAmbush() {
        harness.setHand(player1, List.of(new ReaverAmbush()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Exiles a creature with power 3 or less")
    void exilesCreatureWithPowerThreeOrLess() {
        Permanent target = addCreatureReady(player2, new SunSentinel());
        giveReaverAmbush();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player2, new SunSentinel());
        Permanent target = addCreatureReady(player2, new StampedingHorncrest());
        giveReaverAmbush();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent target = addCreatureReady(player2, new SunSentinel());
        giveReaverAmbush();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles a creature with power exactly three")
    void exilesCreatureWithPowerExactlyThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FrilledDeathspitter());
        giveReaverAmbush();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Frilled Deathspitter");
        harness.assertNotInGraveyard(player2, "Frilled Deathspitter");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Can exile its controller's own creature")
    void exilesOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        giveReaverAmbush();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Sun Sentinel");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StriderHarness());
        giveReaverAmbush();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Strider Harness");
    }

    @Test
    @DisplayName("Does not resolve if a response raises the target's power above three")
    void doesNotExileCreatureWhosePowerIncreasesInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunSentinel());
        giveReaverAmbush();
        harness.setHand(player2, List.of(new MomentOfTriumph()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Reaver Ambush");
    }

    @Test
    @DisplayName("Cannot target a creature whose current power exceeds three after a boost")
    void cannotTargetBoostedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        harness.setHand(player1, List.of(new MomentOfTriumph()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        giveReaverAmbush();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Can exile a creature with negative power")
    void exilesCreatureWithNegativePower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunSentinel());
        target.setPowerModifier(-3);
        giveReaverAmbush();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Sun Sentinel");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(target.getCard().getId()));
    }
}
