package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfVapor.class, GlorySeeker.class, Island.class})
class ChainOfVaporTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a nonland permanent to its owner's hand")
    void returnsNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        castAt(target.getId());

        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertInHand(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("The bounced permanent's controller may sacrifice a land")
    void targetControllerMaySacrificeLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castAt(target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
    }

    @Test
    @DisplayName("Sacrificing a land lets its controller copy Chain of Vapor")
    void sacrificingLandCreatesControllerCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Declining the sacrifice creates no copy")
    void decliningSacrificeCreatesNoCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Declining the copy after sacrificing a land leaves only the original spell's result")
    void decliningCopyAfterSacrificingLandCreatesNoCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castAt(target.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Glory Seeker");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("The controller may retarget the copy at another nonland permanent")
    void copyMayChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        castAt(originalTarget.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(newTarget.getId())
                .doesNotContain(originalTarget.getId(), land.getId());
        harness.handlePermanentChosen(player2, newTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Glory Seeker");
        harness.assertInHand(player1, "Glory Seeker");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Without a land, the bounced permanent's controller is not prompted")
    void noLandMeansNoSacrificePrompt() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GlorySeeker());
        castAt(target.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfVapor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ChainOfVapor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
