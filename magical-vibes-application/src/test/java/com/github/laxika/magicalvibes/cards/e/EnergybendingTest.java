package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Energybending.class, Forest.class, Mountain.class, GrizzlyBears.class})
class EnergybendingTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled lands gain all basic land types and the spell draws a card")
    void controlledLandsGainAllBasicLandTypesAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Mountain()));
        castEnergybending();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, opponentForest))
                .containsExactly(CardSubtype.FOREST);
        assertThat(gqs.effectiveBasicLandTypes(gd, bears)).isEmpty();
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("A newly controlled land is not affected by the resolved spell")
    void newlyControlledLandIsNotAffected() {
        harness.addToBattlefield(player1, new Forest());
        castEnergybending();

        Permanent laterMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThat(gqs.effectiveBasicLandTypes(gd, laterMountain))
                .containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("The temporary type grants and mana abilities expire at end of turn")
    void grantsExpireAtEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castEnergybending();

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIndex, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        forest.resetModifiers();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactly(CardSubtype.FOREST);
    }

    private void castEnergybending() {
        harness.setHand(player1, List.of(new Energybending()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
