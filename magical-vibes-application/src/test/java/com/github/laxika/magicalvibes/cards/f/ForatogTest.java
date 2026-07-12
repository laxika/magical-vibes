package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ForatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest gives Foratog +2/+2")
    void sacrificingForestBoostsForatog() {
        Permanent foratog = addReadyForatog(player1);
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(foratog.getEffectivePower()).isEqualTo(3);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Can activate multiple times, sacrificing multiple Forests")
    void canActivateMultipleTimes() {
        Permanent foratog = addReadyForatog(player1);
        Permanent forest1 = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Two Forests present → prompted which to sacrifice.
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, forest1.getId());
        harness.passBothPriorities();
        // Only one Forest left → auto-sacrificed.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(foratog.getEffectivePower()).isEqualTo(5);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent foratog = addReadyForatog(player1);
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(foratog.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(foratog.getEffectivePower()).isEqualTo(1);
        assertThat(foratog.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without the {G} mana")
    void cannotActivateWithoutMana() {
        addReadyForatog(player1);
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        // No mana added.

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyForatog(Player player) {
        Foratog card = new Foratog();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
