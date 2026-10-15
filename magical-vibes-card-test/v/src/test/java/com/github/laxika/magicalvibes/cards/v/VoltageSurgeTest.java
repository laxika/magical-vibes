package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GreaterTanuki;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoltageSurge.class, HillGiant.class, Spellbook.class, Island.class, GrizzlyBears.class,
        GreaterTanuki.class, LiquimetalCoating.class, NetworkTerminal.class, TezzeretBetrayerOfFlesh.class})
class VoltageSurgeTest extends BaseCardTest {

    @Test
    void dealsTwoDamageWhenArtifactIsNotSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void dealsFourDamageWhenArtifactIsSacrificed() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void cannotTargetAPermanentThatIsNotACreatureOrPlaneswalker() {
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    void cannotSacrificeANonartifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, target.getId(), nonartifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDeclineSacrificeWhileControllingAnArtifact() {
        harness.addToBattlefield(player1, new NetworkTerminal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterTanuki());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Network Terminal");
    }

    @Test
    void sacrificedArtifactLeavesBeforeResolutionAndDealsExactlyFourDamage() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterTanuki());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());

        harness.assertInGraveyard(player1, "Network Terminal");
        harness.assertNotOnBattlefield(player1, "Network Terminal");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Greater Tanuki");
    }

    @Test
    void dealsTwoDamageToPlaneswalkerWithoutSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
    }

    @Test
    void dealsFourDamageToPlaneswalkerWithSacrifice() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
        harness.assertInGraveyard(player2, "Tezzeret, Betrayer of Flesh");
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new NetworkTerminal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterTanuki());
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, target.getId(), artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Network Terminal");
    }

    @Test
    void sacrificingPermanentMadeIntoArtifactPaysTheAdditionalCost() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterTanuki());
        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new VoltageSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Greater Tanuki");
    }
}
