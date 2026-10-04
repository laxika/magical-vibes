package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThroneOfBone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fleshbag Marauder")
@CardUsed({FleshbagMarauder.class, GrizzlyBears.class, GiantSpider.class, ThroneOfBone.class})
class FleshbagMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player sacrifice their only creature automatically")
    void etbMakesEachPlayerSacrifice() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupAndCast();
        harness.passBothPriorities(); // Resolve creature → ETB trigger on stack
        harness.passBothPriorities(); // Resolve ETB → each player sacrifices

        // Controller's only creature is Fleshbag Marauder itself, so it is sacrificed.
        harness.assertNotOnBattlefield(player1, "Fleshbag Marauder");
        harness.assertInGraveyard(player1, "Fleshbag Marauder");
        // Opponent's only creature is sacrificed too.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A player with multiple creatures chooses which to sacrifice")
    void playerWithMultipleCreaturesChooses() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // The opponent with two creatures must choose which one to sacrifice.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
    }

    @Test
    @DisplayName("Answering the sacrifice choice resumes and clears the parked ETB resolution")
    void answeringSacrificeChoiceClearsParkedResolution() {
        GameData gd = harness.getGameData();
        Permanent p2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, p2Bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry)
                .withFailMessage("dangling pendingEffectResolutionEntry after ETB sacrifice flow")
                .isNull();
    }

    @Test
    @DisplayName("ETB sacrifice flow survives an interleaved may-pay cast trigger (Throne of Bone)")
    @CardUsed(ThroneOfBone.class)
    void etbSacrificeSurvivesInterleavedMayTrigger() {
        GameData gd = harness.getGameData();
        // The opponent owns a "whenever a player casts a black spell, you may pay {1}" trigger
        // plus two creatures, so both the may-pay prompt and a real sacrifice choice interleave
        // with the Marauder's parked ETB resolution (fuzz-found dangling-park scenario).
        harness.addToBattlefield(player2, new ThroneOfBone());
        Permanent p2Bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        setupAndCast();
        harness.passBothPriorities(); // Throne of Bone's cast trigger resolves → may-pay prompt
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities(); // Fleshbag Marauder resolves → ETB trigger on stack
        harness.passBothPriorities(); // ETB resolves → sacrifice choices

        // The opponent chooses the Bears; both players must ultimately sacrifice a creature.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, p2Bears.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry)
                .withFailMessage("dangling pendingEffectResolutionEntry after ETB sacrifice flow")
                .isNull();
        harness.assertInGraveyard(player1, "Fleshbag Marauder");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No creature is sacrificed until the opponent has made their choice")
    void sacrificesWaitForAllChoices() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fleshbag Marauder");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");

        harness.handlePermanentChosen(player2, bears.getId());

        harness.assertInGraveyard(player1, "Fleshbag Marauder");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Controller may sacrifice another creature and keep the Marauder")
    void controllerCanKeepMarauder() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        setupAndCast();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fleshbag Marauder");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent with no creatures does not prevent the controller's sacrifice")
    void opponentWithNoCreaturesIsSkipped() {
        harness.addToBattlefield(player2, new ThroneOfBone());

        setupAndCast();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fleshbag Marauder");
        harness.assertOnBattlefield(player2, "Throne of Bone");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new FleshbagMarauder(), "{2}{B}");
    }
}
