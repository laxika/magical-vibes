package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KnightLuminary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CantorOfTheRefrain.class, KnightLuminary.class})
class CantorOfTheRefrainTest extends BaseCardTest {

    @Test
    void cantorCannotBlock() {
        Permanent cantor = addCreatureReady(player1, new CantorOfTheRefrain());

        assertThat(bls.canBlock(gd, cantor)).isFalse();
    }

    @Test
    void warpCastReturnsCantorAndPerpetuallyBoostsIt() {
        CantorOfTheRefrain cantor = new CantorOfTheRefrain();
        harness.setGraveyard(player1, List.of(cantor));
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Cantor of the Refrain");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(cantor);
    }

    @Test
    void normalCastDoesNotReturnCantor() {
        CantorOfTheRefrain cantor = new CantorOfTheRefrain();
        harness.setGraveyard(player1, List.of(cantor));
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(cantor.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cantor);
    }
}
