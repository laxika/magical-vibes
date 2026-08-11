package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PaleBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WinnowingTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new Winnowing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The caster chooses one creature per player and sacrifices non-sharing creatures")
    void choosesCreatureForEachPlayer() {
        Permanent chosenBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player1, new PaleBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new ForestBear());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenBear.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(chosenBear, otherBear)
                .doesNotContain(giant);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(opponentBear)
                .doesNotContain(opponentGiant);
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Changeling shares a creature type with the chosen creature")
    void changelingSharesCreatureType() {
        Permanent chosenBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        cast();

        harness.handleMultiplePermanentsChosen(player1, List.of(chosenBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(chosenBear, changeling)
                .doesNotContain(giant);
        harness.assertInGraveyard(player1, "Hill Giant");
    }
}
