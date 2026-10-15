package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Aladdin.class, GrizzlyBears.class, LeoninScimitar.class, Unsummon.class, Persuasion.class})
class AladdinTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of target artifact for as long as Aladdin remains under your control")
    void gainsControlOfTargetArtifact() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("The artifact returns when Aladdin leaves the battlefield")
    void artifactReturnsWhenAladdinLeavesBattlefield() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, aladdin.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aladdin), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("No control is gained if Aladdin leaves before the ability resolves")
    void sourceLeavesBeforeResolution() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, aladdin.getId());
        harness.assertInHand(player1, "Aladdin");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Untapping Aladdin does not end control of the artifact")
    void controlPersistsWhenAladdinUntaps() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);

        assertThat(aladdin.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Losing control of Aladdin returns the artifact even though Aladdin remains")
    void artifactReturnsWhenAladdinChangesController() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Persuasion()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, aladdin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(aladdin, artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aladdin, artifact);
    }

    @Test
    @DisplayName("Aladdin cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent aladdin = harness.addToBattlefieldAndReturn(player1, new Aladdin());
        Permanent artifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, aladdin), null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Aladdin can target an artifact already under your control and pays the tap cost")
    void canTargetOwnArtifact() {
        Permanent aladdin = addReadyAladdin(player1);
        Permanent artifact = addArtifact(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, aladdin), null, artifact.getId());
        assertThat(aladdin.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    private Permanent addReadyAladdin(Player player) {
        return addCreatureReady(player, new Aladdin());
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
