package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThranWeaponryTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability boosts all creatures on both battlefields")
    void activatedAbilityBoostsAllCreatures() {
        Permanent weaponry = addReadyWeaponry();
        Permanent ownBear = addReadyBear(player1);
        Permanent opponentBear = addReadyBear(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();

        assertThat(weaponry.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost is locked to creatures present when the ability resolves")
    void boostDoesNotAffectLaterCreatures() {
        Permanent weaponry = addReadyWeaponry();
        Permanent existingBear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();
        Permanent laterBear = addReadyBear(player2);

        assertThat(gqs.getEffectivePower(gd, existingBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost ends when the artifact becomes untapped")
    void boostEndsWhenWeaponryUntaps() {
        Permanent weaponry = addReadyWeaponry();
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaponry), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(weaponry.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining echo sacrifices Thran Weaponry at its next upkeep")
    void decliningEchoSacrificesWeaponry() {
        castAndResolveWeaponry();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Thran Weaponry");
        harness.assertInGraveyard(player1, "Thran Weaponry");
    }

    @Test
    @DisplayName("Paying echo keeps Thran Weaponry and echo does not trigger again")
    void payingEchoKeepsWeaponryAndEchoIsOneShot() {
        castAndResolveWeaponry();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Thran Weaponry");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Thran Weaponry");
    }

    private Permanent addReadyWeaponry() {
        Permanent weaponry = harness.addToBattlefieldAndReturn(player1, new ThranWeaponry());
        weaponry.setSummoningSick(false);
        return weaponry;
    }

    private Permanent addReadyBear(Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setSummoningSick(false);
        return bear;
    }

    private void castAndResolveWeaponry() {
        harness.setHand(player1, List.of(new ThranWeaponry()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thran Weaponry");
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
