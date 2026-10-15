package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CravenHulk;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.m.MaskedVandal;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Squash.class, CravenHulk.class, FearlessPup.class, SnowCoveredForest.class, TyvarKell.class,
        MaskedVandal.class})
class SquashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a creature")
    void dealsSixDamageToCreature() {
        harness.addToBattlefield(player2, new CravenHulk());
        prepareCast(4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Craven Hulk"));

        harness.assertInGraveyard(player2, "Craven Hulk");
    }

    @Test
    @DisplayName("Deals 6 damage to a planeswalker")
    void dealsSixDamageToPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 6);
        prepareCast(4);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("Costs {3} less to cast when you control a Giant")
    void costsThreeLessWithGiant() {
        harness.addToBattlefield(player1, new CravenHulk());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.setHand(player1, List.of(new Squash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fearless Pup"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the reduced cost without controlling a Giant")
    void reducedCostRequiresGiant() {
        harness.addToBattlefield(player1, new FearlessPup());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.setHand(player1, List.of(new Squash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Fearless Pup")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        prepareCast(4);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Snow-Covered Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals exactly six damage to a planeswalker with seven loyalty")
    void leavesOneLoyaltyAfterSixDamage() {
        Permanent planeswalker = addPlaneswalker(player2, 7);
        prepareCast(4);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Tyvar Kell");
    }

    @Test
    @DisplayName("An opponent's Giant does not reduce the cost")
    void opposingGiantDoesNotReduceCost() {
        harness.addToBattlefield(player2, new CravenHulk());
        prepareCast(1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Craven Hulk")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Giants still reduce the cost by only three")
    void multipleGiantsDoNotStackReduction() {
        harness.addToBattlefield(player1, new CravenHulk());
        harness.addToBattlefield(player1, new CravenHulk());
        harness.addToBattlefield(player2, new FearlessPup());
        prepareCast(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Fearless Pup"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Giant reduction does not remove the red mana requirement")
    void reducedCostStillRequiresRedMana() {
        harness.addToBattlefield(player1, new CravenHulk());
        harness.addToBattlefield(player2, new FearlessPup());
        harness.setHand(player1, List.of(new Squash()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Fearless Pup")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCast(4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target your own Giant and receive the cost reduction")
    void canTargetOwnGiantAtReducedCost() {
        harness.addToBattlefield(player1, new CravenHulk());
        prepareCast(1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Craven Hulk"));

        harness.assertInGraveyard(player1, "Craven Hulk");
        harness.assertInGraveyard(player1, "Squash");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A creature with changeling qualifies as a Giant")
    void changelingReducesCost() {
        harness.addToBattlefield(player1, new MaskedVandal());
        harness.addToBattlefield(player2, new FearlessPup());
        prepareCast(1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Fearless Pup"));

        harness.assertInGraveyard(player2, "Fearless Pup");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void prepareCast(int genericMana) {
        harness.setHand(player1, List.of(new Squash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TyvarKell());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
