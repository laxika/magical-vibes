package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GerrardsCommand.class, AuroraGriffin.class, ManaCylix.class})
class GerrardsCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gerrard's Command untaps and boosts the target creature")
    void untapsAndBoostsTargetCreature() {
        Permanent target = addTappedCreature();
        castGerrardsCommand(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("An already untapped target creature still gets the boost")
    void boostsAlreadyUntappedTargetCreature() {
        Permanent target = addCreatureReady(player2, new AuroraGriffin());
        castGerrardsCommand(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addTappedCreature();
        castGerrardsCommand(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent creature = addTappedCreature();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ManaCylix());
        harness.setHand(player1, List.of(new GerrardsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap and boost your own creature without affecting other creatures")
    void affectsOnlyTheTargetedCreature() {
        Permanent target = addCreatureReady(player1, new AuroraGriffin());
        target.tap();
        Permanent other = addTappedCreature();

        castGerrardsCommand(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Neither effect applies when the target leaves before resolution")
    void targetLeavingBattlefieldPreventsBothEffects() {
        Permanent target = addTappedCreature();
        Permanent other = addTappedCreature();
        harness.setHand(player1, List.of(new GerrardsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GerrardsCommand);
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addTappedCreature() {
        Permanent creature = addCreatureReady(player2, new AuroraGriffin());
        creature.tap();
        return creature;
    }

    private void castGerrardsCommand(Permanent target) {
        harness.setHand(player1, List.of(new GerrardsCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
