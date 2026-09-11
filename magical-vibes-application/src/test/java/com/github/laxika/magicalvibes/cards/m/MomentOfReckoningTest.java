package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MomentOfReckoningTest extends BaseCardTest {

    @Test
    void destroysTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotDestroyLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTargetNonlandPermanentCardFromGraveyard() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        cast(new int[]{1}, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canChooseTheSameModeMoreThanOnce() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0, 0}, List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    void canChooseBothModesWithMixedTargets() {
        Permanent permanentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardTarget = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(graveyardTarget)));
        cast(new int[]{0, 1}, List.of(permanentTarget.getId(), graveyardTarget.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayChooseNoModes() {
        harness.setHand(player1, List.of(new MomentOfReckoning()));
        addMana();

        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelectionInRange(0, 4, 2), null, null, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Moment of Reckoning"));
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new MomentOfReckoning()));
        addMana();
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelectionInRange(0, 4, 2, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
