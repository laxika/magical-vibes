package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OxiddaDaredevil.class, Spellbook.class, Memnite.class})
class OxiddaDaredevilTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability grants haste to Oxidda Daredevil on resolution")
    void grantsHasteOnResolution() {
        Permanent daredevil = harness.addToBattlefieldAndReturn(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Ability can be activated even when summoning sick (no tap cost)")
    void canActivateWhenSummoningSick() {
        Permanent daredevil = harness.addToBattlefieldAndReturn(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());
        assertThat(daredevil.isSummoningSick()).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrificed artifact goes to graveyard")
    void sacrificedArtifactGoesToGraveyard() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Haste is granted only on resolution and expires at end of turn")
    void hasteExpiresAtEndOfTurn() {
        Permanent daredevil = harness.addToBattlefieldAndReturn(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player1, new Memnite());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Memnite");
        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passBothPriorities();
        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new OxiddaDaredevil());
        harness.addToBattlefield(player2, new Memnite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Memnite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses which artifact to sacrifice when several are available")
    void choosesArtifactToSacrifice() {
        Permanent daredevil = harness.addToBattlefieldAndReturn(player1, new OxiddaDaredevil());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Memnite());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Memnite");
        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(daredevil.hasKeyword(Keyword.HASTE)).isTrue();
    }

}
