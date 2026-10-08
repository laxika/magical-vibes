package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromeDome.class, Ornithopter.class, GrizzlyBears.class, LeoninScimitar.class, Clone.class})
class ChromeDomeTest extends BaseCardTest {

    @Test
    @DisplayName("Other artifact creatures you control get +1/+0")
    void buffsOtherArtifactCreatures() {
        harness.addToBattlefield(player1, new ChromeDome());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent dome = findPermanent(player1, "Chrome Dome");
        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentOrnithopter = findPermanent(player2, "Ornithopter");

        assertThat(gqs.getEffectivePower(gd, dome)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentOrnithopter)).isEqualTo(0);
    }

    @Test
    @DisplayName("Copies another artifact you control into a hasty token")
    void copiesAnotherArtifactYouControl() {
        addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Leonin Scimitar");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifices the copy at the beginning of the next end step")
    void sacrificesCopyAtNextEndStep() {
        addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void canActivateWhileSummoningSick() {
        Permanent dome = harness.addToBattlefieldAndReturn(player1, new ChromeDome());
        dome.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dome.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void canActivateWhileTapped() {
        Permanent dome = addReadyDome(player1);
        dome.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void canActivateRepeatedlyWithoutUntapping() {
        Permanent dome = addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dome.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    void sacrificeWaitsForDelayedTriggerToResolve() {
        addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void grantedHasteIsNotCopiedByClone() {
        addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, clone, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, clone, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clone)).isEqualTo(1);
    }

    @Test
    void missingTargetDoesNotCreateToken() {
        addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copyResolvesAfterChromeDomeLeaves() {
        Permanent dome = addReadyDome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, dome);

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isZero();
    }

    @Test
    @DisplayName("Cannot target itself, an opponent's artifact, or a nonartifact permanent")
    void targetMustBeAnotherArtifactYouControl() {
        Permanent dome = addReadyDome(player1);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 15);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dome.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDome(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChromeDome());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
