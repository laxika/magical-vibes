package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({UnfortunateAccident.class, GrizzlyBears.class})
class UnfortunateAccidentTest extends BaseCardTest {

    @Test
    @DisplayName("The destroy mode destroys a target creature")
    void destroysTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2, 2);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The token mode creates a Mercenary with its sorcery-speed boost ability")
    void createsMercenaryWithBoostAbility() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        cast(new int[]{1}, List.of(), 1, 1);

        Permanent mercenary = findPermanent(player1, "Mercenary");
        mercenary.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spree resolves both modes and charges both additional costs")
    void resolvesBothModes() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(target.getId()), 2, 3);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The destroy mode rejects a player target")
    void rejectsNonCreatureTarget() {
        assertThatThrownBy(() -> cast(new int[]{0}, List.of(player2.getId()), 2, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int blackMana, int colorlessMana) {
        harness.setHand(player1, List.of(new UnfortunateAccident()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }
}
