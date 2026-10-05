package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SteelGolem;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MechanizedWarfare.class, Shock.class, Blaze.class, SteelGolem.class,
        GrizzlyBears.class, SerraAngel.class, TeferiTemporalPilgrim.class})
class MechanizedWarfareTest extends BaseCardTest {

    @Test
    @DisplayName("Red spell deals one extra damage to an opponent")
    void redSpellDealsExtraDamageToOpponent() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Artifact creature deals one extra combat damage to an opponent")
    void artifactCreatureDealsExtraCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MechanizedWarfare());
        addCreatureReady(player1, new SteelGolem());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Red spell deals one extra damage to an opponent's permanent")
    void redSpellDealsExtraDamageToOpponentsPermanent() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        UUID serraId = harness.getPermanentId(player2, "Serra Angel");

        harness.castSorcery(player1, 0, 3, serraId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Nonred nonartifact creature gets no bonus")
    void nonredNonartifactCreatureGetsNoBonus() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MechanizedWarfare());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to the controller is not increased")
    void damageToControllerIsNotIncreased() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Red spell deals extra damage to an opponent's planeswalker")
    void redSpellDealsExtraDamageToOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.addToBattlefield(player2, new TeferiTemporalPilgrim());
        var teferi = findPermanent(player2, "Teferi, Temporal Pilgrim");
        teferi.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, teferi.getId());
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple copies each increase damage")
    void multipleCopiesIncreaseDamage() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("An opponent's red source gets no bonus")
    void opponentsRedSourceGetsNoBonus() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Damage to a friendly permanent is not increased")
    void damageToFriendlyPermanentIsNotIncreased() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        var serra = findPermanent(player1, "Serra Angel");

        harness.castSorcery(player1, 0, 3, serra.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(serra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Zero damage is not increased")
    void zeroDamageIsNotIncreased() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Artifact combat damage to an opposing creature is increased")
    void artifactCombatDamageToOpposingCreatureIsIncreased() {
        harness.addToBattlefield(player1, new MechanizedWarfare());
        addCreatureReady(player1, new SteelGolem());
        harness.addToBattlefield(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat(player1);

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Steel Golem");
    }
}
