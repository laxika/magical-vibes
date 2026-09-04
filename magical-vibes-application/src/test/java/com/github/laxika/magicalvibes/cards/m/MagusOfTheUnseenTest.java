package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcticFoxes;
import com.github.laxika.magicalvibes.cards.i.InfernalDenizen;
import com.github.laxika.magicalvibes.cards.s.SnowFortress;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheUnseen.class, UrzasBauble.class, ArcticFoxes.class,
        InfernalDenizen.class, SnowFortress.class})
class MagusOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating untaps the opponent's artifact and gains control of it")
    void activatingStealsAndUntapsArtifact() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrzasBauble());
        artifact.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(magus.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("At end of turn the artifact returns to its owner tapped")
    void artifactReturnsTappedAtEndOfTurn() {
        // Run this on the artifact owner's (player2's) turn so the cleanup control-revert is
        // observable before player2's next untap step would clear the tap.
        harness.forceActivePlayer(player2);
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrzasBauble());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Taps the artifact when another effect takes it before end of turn")
    void artifactIsTappedWhenAnotherEffectTakesControl() {
        harness.forceActivePlayer(player2);
        addCreatureReady(player1, new MagusOfTheUnseen());
        addCreatureReady(player2, new MagusOfTheUnseen());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SnowFortress());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, null, artifact.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not tap the artifact at cleanup if another effect keeps your control")
    void doesNotTapWhenAnotherControlEffectKeepsControl() {
        harness.forceActivePlayer(player1);
        addCreatureReady(player1, new MagusOfTheUnseen());
        addCreatureReady(player1, new InfernalDenizen());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SnowFortress());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(artifact.getId()));
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an artifact you control")
    void cannotTargetOwnArtifact() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new UrzasBauble());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addCreatureReady(player1, new MagusOfTheUnseen());
        Permanent creature = addCreatureReady(player2, new ArcticFoxes());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }
}
