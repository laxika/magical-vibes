package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gnathosaur.class, Spellbook.class, LeoninScimitar.class})
class GnathosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact grants Gnathosaur trample until end of turn")
    void sacrificeArtifactGrantsTrample() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Artifact should be sacrificed
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");

        // Gnathosaur should have trample
        assertThat(gnathosaur.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Trample granted by ability resets at end of turn")
    void trampleResetsAtEndOfTurn() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gnathosaur.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gnathosaur.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Activating with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addReadyGnathosaur(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Does not require tap or mana to activate")
    void noManaCostNoTapRequired() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        gnathosaur.tap();
        harness.addToBattlefield(player1, new Spellbook());

        // No mana added, gnathosaur is tapped — should still work
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate multiple times per turn with multiple artifacts")
    void canActivateMultipleTimes() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        UUID spellbookId = findPermanent(player1, "Spellbook").getId();

        // First activation: 2 artifacts, must choose
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, spellbookId);
        harness.passBothPriorities();

        assertThat(gnathosaur.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        // Second activation: 1 artifact left, auto-sacrificed
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Still has trample (granted twice, both active)
        assertThat(gnathosaur.getGrantedKeywords()).contains(Keyword.TRAMPLE);

        // Both artifacts should be gone
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Artifact is sacrificed immediately, but only the source gains trample on resolution")
    void sacrificeIsPaidBeforeTrampleResolves() {
        Permanent source = addReadyGnathosaur(player1);
        Permanent other = addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(source.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);

        harness.passBothPriorities();

        assertThat(source.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent activating the sacrifice ability")
    void canActivateWhileSummoningSick() {
        Permanent gnathosaur = harness.addToBattlefieldAndReturn(player1, new Gnathosaur());
        gnathosaur.setSummoningSick(true);
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gnathosaur.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        harness.addToBattlefield(player2, new Spellbook());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.stack).isEmpty();
        assertThat(gnathosaur.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    private Permanent addReadyGnathosaur(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Gnathosaur());
        perm.setSummoningSick(false);
        return perm;
    }

}
