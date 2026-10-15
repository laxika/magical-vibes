package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.j.JanglingAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranForge.class, BenalishKnight.class, JanglingAutomaton.class, AuraOfSilence.class})
class ThranForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target nonartifact creature +1/+0 and makes it an artifact until end of turn")
    void buffsAndMakesTargetArtifact() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
    }

    @Test
    @DisplayName("Can target a nonartifact creature controlled by its controller")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent target = addCreatureReady(player1, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("The boost and artifact type wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.isArtifact(target)).isFalse();
    }

    @Test
    @DisplayName("The ability fizzles if its target becomes an artifact before resolution")
    void fizzlesIfTargetBecomesArtifactBeforeResolution() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.isArtifact(target)).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent artifactCreature = addCreatureReady(player2, new JanglingAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonartifact creature");
    }

    @Test
    @DisplayName("Can activate a tapped Forge without tapping it as a cost")
    void canActivateWhileTapped() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new ThranForge());
        forge.tap();
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(forge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without paying two mana")
    void cannotActivateWithOnlyOneMana() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.isArtifact(target)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new ThranForge());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonartifact creature");
    }

    @Test
    @DisplayName("The ability resolves even if the Forge is destroyed in response")
    void resolvesAfterSourceIsDestroyed() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new ThranForge());
        Permanent target = addCreatureReady(player2, new BenalishKnight());
        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.sacrificePermanent(player2, 1, forge.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Thran Forge");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.isArtifact(target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
    }
}
