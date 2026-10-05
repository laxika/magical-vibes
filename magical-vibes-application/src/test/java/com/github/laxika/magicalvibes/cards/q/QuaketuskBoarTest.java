package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuaketuskBoar.class, GrizzlyBears.class, SuntailHawk.class})
class QuaketuskBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Quaketusk Boar to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setHand(player1, List.of(new QuaketuskBoar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Reach allows Quaketusk Boar to block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addReadyPermanent(player1, new SuntailHawk());
        Permanent boar = addReadyPermanent(player2, new QuaketuskBoar());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(boar),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(boar.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        Permanent boar = addReadyPermanent(player1, new QuaketuskBoar());
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(boar))));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before assigning excess damage")
    void trampleRequiresLethalDamageBeforeOverflow() {
        addReadyPermanent(player1, new QuaketuskBoar());
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({QuaketuskBoar.class})
    @DisplayName("Trample deals no damage to the defending player when the blocker absorbs all damage")
    void noOverflowWhenBlockerRequiresAllDamage() {
        addReadyPermanent(player1, new QuaketuskBoar());
        Permanent blocker = addReadyPermanent(player2, new QuaketuskBoar());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player,
                                         com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
