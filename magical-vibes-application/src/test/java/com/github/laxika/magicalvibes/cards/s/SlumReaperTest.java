package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Slum Reaper")
class SlumReaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice their only creature automatically")
    void etbMakesEachPlayerSacrifice() {
        harness.getGameData().playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        setupAndCast();
        harness.passBothPriorities(); // Resolve creature → ETB trigger on stack
        harness.passBothPriorities(); // Resolve ETB → each player sacrifices

        // The controller's only creature is Slum Reaper itself, so it sacrifices itself.
        harness.assertNotOnBattlefield(player1, "Slum Reaper");
        harness.assertInGraveyard(player1, "Slum Reaper");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        GameData gd = harness.getGameData();
        Permanent p2Bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(p2Bears);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GiantSpider()));

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, p2Bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
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

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SlumReaper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
