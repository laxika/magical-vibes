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

    @Test
    void warpCastDoesNotReturnOpponentsCantor() {
        CantorOfTheRefrain cantor = new CantorOfTheRefrain();
        harness.setGraveyard(player2, List.of(cantor));
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cantor of the Refrain");
        harness.assertNotOnBattlefield(player2, "Cantor of the Refrain");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(cantor);
    }

    @Test
    void warpCastReturnsAndBoostsEachGraveyardCopyOnlyOnce() {
        CantorOfTheRefrain first = new CantorOfTheRefrain();
        CantorOfTheRefrain second = new CantorOfTheRefrain();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cantor of the Refrain"))
                .hasSize(2)
                .allSatisfy(returned -> {
                    assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(1);
                    assertThat(returned.isTapped()).isFalse();
                    assertThat(bls.canBlock(gd, returned)).isFalse();
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void warpCastDoesNotBoostCantorAlreadyOnBattlefield() {
        Permanent cantor = addCreatureReady(player1, new CantorOfTheRefrain());
        harness.setHand(player1, List.of(new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, cantor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cantor)).isEqualTo(1);
    }

    @Test
    void perpetualBoostSurvivesDeathAndAccumulatesOnAnotherReturn() {
        harness.setGraveyard(player1, List.of(new CantorOfTheRefrain()));
        harness.setHand(player1, List.of(new KnightLuminary(), new KnightLuminary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        Permanent firstReturn = findPermanent(player1, "Cantor of the Refrain");
        assertThat(gqs.getEffectivePower(gd, firstReturn)).isEqualTo(3);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, firstReturn));
        harness.assertInGraveyard(player1, "Cantor of the Refrain");

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent secondReturn = findPermanent(player1, "Cantor of the Refrain");
        assertThat(gqs.getEffectivePower(gd, secondReturn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondReturn)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Cantor of the Refrain");
    }
}
