package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeamRip.class, Forest.class, GrizzlyBears.class, HillGiant.class, Naturalize.class})
class SeamRipTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opposing nonland permanent with mana value 2 or less")
    void etbExilesEligibleOpponentPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Seam Rip leaves the battlefield")
    void exiledPermanentReturnsWhenSourceLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID seamRipId = harness.getPermanentId(player1, "Seam Rip");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, seamRipId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castAndResolve(forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than 2")
    void cannotTargetPermanentWithHighManaValue() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> castAndResolve(giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castAndResolve(bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Seam Rip before its trigger resolves leaves the target on the battlefield")
    void sourceLeavingBeforeTriggerResolvesDoesNotExileTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeamRip()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Seam Rip"));
        harness.assertNotOnBattlefield(player1, "Seam Rip");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(bears.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Seam Rip can exile an opposing enchantment")
    void exilesEligibleNoncreaturePermanent() {
        Permanent opposingSeamRip = harness.addToBattlefieldAndReturn(player2, new SeamRip());

        castAndResolve(opposingSeamRip.getId());

        harness.assertNotOnBattlefield(player2, "Seam Rip");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opposingSeamRip.getCard().getId()));
        harness.assertOnBattlefield(player1, "Seam Rip");
    }

    private void castAndResolve(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SeamRip()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
