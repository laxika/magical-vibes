package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrborgTombOfYawgmothTest extends BaseCardTest {

    @Test
    @DisplayName("Every land keeps its own types and becomes a Swamp as well")
    void allLandsBecomeSwampsAdditively() {
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, ownForest))
                .contains(CardSubtype.FOREST, CardSubtype.SWAMP);
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentForest))
                .contains(CardSubtype.FOREST, CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A land under Urborg taps for black as well as its own color")
    void landsTapForBlack() {
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        gs.tapPermanent(gd, player1, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Urborg is a Swamp itself and taps for black")
    void urborgIsItselfASwamp() {
        Permanent urborg = harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());

        assertThat(gqs.effectiveBasicLandTypes(gd, urborg)).contains(CardSubtype.SWAMP);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands stop being Swamps once Urborg leaves the battlefield")
    void grantEndsWhenUrborgLeaves() {
        Permanent urborg = harness.addToBattlefieldAndReturn(player1, new UrborgTombOfYawgmoth());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.SWAMP);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, urborg));

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }
}
