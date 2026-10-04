package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
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

@CardUsed({FrostBite.class, GarrukWildspeaker.class, HillGiant.class, IcehideTroll.class,
        SnowCoveredForest.class, SnowCoveredIsland.class, SnowCoveredMountain.class})
class FrostBiteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage without three snow permanents")
    void dealsTwoDamageWithoutThreeSnowPermanents() {
        harness.addToBattlefield(player2, new HillGiant());
        castFrostBite(harness.getPermanentId(player2, "Hill Giant"));

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 3 damage with three snow permanents")
    void dealsThreeDamageWithThreeSnowPermanents() {
        addThreeSnowPermanents();
        harness.addToBattlefield(player2, new HillGiant());
        castFrostBite(harness.getPermanentId(player2, "Hill Giant"));

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Checks snow permanents when it resolves")
    void checksSnowPermanentsWhenItResolves() {
        addThreeSnowPermanents();
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        addThreeSnowPermanents();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        castFrostBite(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Snow-Covered Forest")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two snow permanents and the snow spell do not meet the threshold")
    void twoSnowPermanentsStillDealTwoDamage() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideTroll());

        castFrostBite(target.getId());

        harness.assertOnBattlefield(player2, "Icehide Troll");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's snow permanents do not increase damage")
    void opponentsSnowPermanentsDoNotCount() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        harness.addToBattlefield(player2, new SnowCoveredIsland());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideTroll());

        castFrostBite(target.getId());

        harness.assertOnBattlefield(player2, "Icehide Troll");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Snow creatures count toward the threshold")
    void snowCreaturesCount() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player1, new IcehideTroll());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideTroll());

        castFrostBite(target.getId());

        harness.assertInGraveyard(player2, "Icehide Troll");
        harness.assertOnBattlefield(player1, "Icehide Troll");
    }

    @Test
    @DisplayName("More than three snow permanents still deals only 3 damage")
    void moreThanThreeSnowPermanentsDealThreeDamage() {
        addThreeSnowPermanents();
        harness.addToBattlefield(player1, new SnowCoveredForest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideTroll());

        castFrostBite(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Icehide Troll");
    }

    @Test
    @DisplayName("Snow permanents gained before resolution increase damage")
    void gainingThirdSnowPermanentBeforeResolutionIncreasesDamage() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcehideTroll());
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());

        harness.addToBattlefield(player1, new SnowCoveredMountain());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Icehide Troll");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IcehideTroll());

        castFrostBite(target.getId());

        harness.assertOnBattlefield(player1, "Icehide Troll");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals 2 damage to a planeswalker without snow permanents")
    void dealsTwoDamageToPlaneswalkerWithoutSnow() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 5);

        castFrostBite(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFrostBite(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addThreeSnowPermanents() {
        harness.addToBattlefield(player1, new SnowCoveredForest());
        harness.addToBattlefield(player1, new SnowCoveredIsland());
        harness.addToBattlefield(player1, new SnowCoveredMountain());
    }

}
