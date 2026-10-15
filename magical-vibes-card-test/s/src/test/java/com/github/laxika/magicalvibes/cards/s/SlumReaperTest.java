package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.c.CatacombSlug;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlumReaper.class, CatacombSlug.class, AxebaneStag.class})
@DisplayName("Slum Reaper")
class SlumReaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice their only creature automatically")
    void etbMakesEachPlayerSacrifice() {
        harness.addToBattlefield(player2, new CatacombSlug());

        setupAndCast();
        harness.passBothPriorities(); // Resolve the creature; its ETB trigger goes on the stack.
        harness.passBothPriorities(); // Resolve the ETB trigger.

        // The controller's only creature is Slum Reaper itself, so it sacrifices itself.
        harness.assertNotOnBattlefield(player1, "Slum Reaper");
        harness.assertInGraveyard(player1, "Slum Reaper");
        harness.assertNotOnBattlefield(player2, "Catacomb Slug");
        harness.assertInGraveyard(player2, "Catacomb Slug");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        GameData gd = harness.getGameData();
        Permanent p2Slug = harness.addToBattlefieldAndReturn(player2, new CatacombSlug());
        harness.addToBattlefield(player2, new AxebaneStag());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).context())
                .isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(p2Slug.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.assertInGraveyard(player2, "Catacomb Slug");
        harness.assertOnBattlefield(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("A player with no creatures sacrifices nothing")
    void playerWithNoCreaturesSacrificesNothing() {
        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Only the Reaper is on the battlefield, so only it dies; the opponent is unaffected.
        harness.assertInGraveyard(player1, "Slum Reaper");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both players choose before any creature is sacrificed")
    void bothPlayersChooseBeforeSacrifices() {
        Permanent controllerSlug = harness.addToBattlefieldAndReturn(player1, new CatacombSlug());
        Permanent opponentSlug = harness.addToBattlefieldAndReturn(player2, new CatacombSlug());
        harness.addToBattlefield(player2, new AxebaneStag());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(controllerSlug.getId()));

        harness.assertOnBattlefield(player1, "Catacomb Slug");
        harness.assertNotInGraveyard(player1, "Catacomb Slug");
        harness.assertOnBattlefield(player2, "Catacomb Slug");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentSlug.getId()));

        harness.assertInGraveyard(player1, "Catacomb Slug");
        harness.assertInGraveyard(player2, "Catacomb Slug");
        harness.assertOnBattlefield(player1, "Slum Reaper");
        harness.assertOnBattlefield(player2, "Axebane Stag");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void setupAndCast() {
        harness.setHand(player1, List.of(new SlumReaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
