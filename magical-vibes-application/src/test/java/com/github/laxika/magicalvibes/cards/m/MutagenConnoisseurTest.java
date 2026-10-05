package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.InvasionOfPyrulea;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfPyrulea.class, MutagenConnoisseur.class})
class MutagenConnoisseurTest extends BaseCardTest {

    @Test
    void getsPlusOnePowerForEachTransformedPermanentControlled() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new MutagenConnoisseur());

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, connoisseur)).isEqualTo(5);

        addTransformedPyrulea(player1);
        assertThat(gqs.getEffectivePower(gd, connoisseur)).isEqualTo(1);

        addTransformedPyrulea(player1);
        assertThat(gqs.getEffectivePower(gd, connoisseur)).isEqualTo(2);
    }

    @Test
    void doesNotCountUntransformedOrOpponentControlledPermanents() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new MutagenConnoisseur());
        harness.addToBattlefield(player1, new InvasionOfPyrulea());
        addTransformedPyrulea(player2);

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isZero();
    }

    @Test
    void bonusDecreasesWhenTransformedPermanentLeavesBattlefield() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new MutagenConnoisseur());
        Permanent transformed = addTransformedPyrulea(player1);
        addTransformedPyrulea(player1);

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isEqualTo(2);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, transformed));

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, connoisseur)).isEqualTo(5);
    }

    @Test
    void bonusTracksCurrentControllerOfTransformedPermanent() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new MutagenConnoisseur());
        Permanent transformed = addTransformedPyrulea(player1);

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(transformed);
        gd.playerBattlefields.get(player2.getId()).add(transformed);

        assertThat(gqs.getEffectivePower(gd, connoisseur)).isZero();
    }
    private Permanent addTransformedPyrulea(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new InvasionOfPyrulea());
        permanent.setCard(permanent.getOriginalCard().getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }
}
