package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostIntercessor;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FinalFlourish;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarshalOfZhalfir;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.cards.t.TidalTerror;
import com.github.laxika.magicalvibes.cards.u.UrnOfGodfire;
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

@CardUsed({LithomanticBarrage.class, Cancel.class, EliteVanguard.class, GrizzlyBears.class,
        MuYanlingSkyDancer.class, AlabasterHostIntercessor.class, ChandraHopesBeacon.class,
        FinalFlourish.class, MarshalOfZhalfir.class, TidalTerror.class, UrnOfGodfire.class})
class LithomanticBarrageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature that is neither white nor blue")
    void dealsOneDamageToOtherColor() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 5 damage to a white creature")
    void dealsFiveDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));

        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 5 damage to a blue planeswalker")
    void dealsFiveDamageToBluePlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        planeswalker.setSummoningSick(false);
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        LithomanticBarrage barrage = new LithomanticBarrage();
        harness.setHand(player1, List.of(barrage));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        LithomanticBarrage barrage = new LithomanticBarrage();
        harness.setHand(player1, List.of(barrage));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, barrage.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Deals exactly 5 damage to a surviving blue creature")
    void dealsExactlyFiveDamageToBlueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TidalTerror());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Tidal Terror");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can target its controller's white creature and deals exactly 5 damage")
    void dealsExactlyFiveDamageToOwnWhiteCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AlabasterHostIntercessor());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Alabaster Host Intercessor");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deals 5 damage to a creature that is both white and blue")
    void dealsFiveDamageToWhiteAndBlueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MarshalOfZhalfir());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Marshal of Zhalfir");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deals only 1 damage to a planeswalker that is neither white nor blue")
    void dealsOneDamageToRedPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Chandra, Hope's Beacon");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not resolve after its only target leaves the battlefield despite being uncounterable")
    void doesNotResolveWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LithomanticBarrage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new FinalFlourish()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lithomantic Barrage");
    }
}
