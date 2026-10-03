package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.v.VivienOnTheHunt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BouncersBeatdown.class, CrawWurm.class, GrizzlyBears.class, HillGiant.class,
        ScatheZombies.class, Murder.class, VivienOnTheHunt.class})
class BouncersBeatdownTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less when targeting a black permanent")
    void costsLessWhenTargetingBlackPermanent() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Requires the full cost when targeting a nonblack permanent")
    void requiresFullCostWhenTargetingNonblackPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest power among creatures you control")
    void dealsGreatestControlledPowerAsDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId())
                        && permanent.getMarkedDamage() == 3);
    }

    @Test
    @DisplayName("Exiles a creature killed by the damage instead of putting it into the graveyard")
    void exilesCreatureKilledByDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertNotInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Scathe Zombies"));
    }

    @Test
    void zeroDamageStillExilesTargetDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new BouncersBeatdown(), new Murder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Scathe Zombies");
        assertThat(target.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertNotInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
    }

    @Test
    void greatestPowerIsDeterminedOnResolution() {
        Permanent strongest = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new BouncersBeatdown(), new Murder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, strongest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Craw Wurm");
    }

    @Test
    void damagesPlaneswalkerByRemovingLoyalty() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VivienOnTheHunt());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Vivien on the Hunt");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void exilesPlaneswalkerWithLethalDamage() {
        harness.addToBattlefield(player1, new CrawWurm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VivienOnTheHunt());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Vivien on the Hunt");
        harness.assertNotInGraveyard(player2, "Vivien on the Hunt");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(target.getCard().getId()));
    }
}
