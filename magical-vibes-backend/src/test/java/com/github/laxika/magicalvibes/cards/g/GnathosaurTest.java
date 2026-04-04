package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GnathosaurTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice-an-artifact activated ability that grants trample")
    void hasCorrectAbilityStructure() {
        Gnathosaur card = new Gnathosaur();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeArtifactCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== Activation: sacrifice an artifact to gain trample =====

    @Test
    @DisplayName("Sacrificing an artifact grants Gnathosaur trample until end of turn")
    void sacrificeArtifactGrantsTrample() {
        Permanent gnathosaur = addReadyGnathosaur(player1);
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Artifact should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));

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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addReadyGnathosaur(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
    }

    // ===== Helpers =====

    private Permanent addReadyGnathosaur(Player player) {
        Gnathosaur card = new Gnathosaur();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
