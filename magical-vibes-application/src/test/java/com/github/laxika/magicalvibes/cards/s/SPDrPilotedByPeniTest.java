package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SPDrPilotedByPeni.class, GrizzlyBears.class})
class SPDrPilotedByPeniTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by putting a +1/+1 counter on a target creature")
    void entersWithTargetCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SPDrPilotedByPeni()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 0, target.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when a modified creature you control deals combat damage to a player")
    void drawsForModifiedCreatureCombatDamage() {
        addReadyCreature(player1, new SPDrPilotedByPeni());
        Permanent modifiedAttacker = addReadyCreature(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for an unmodified creature dealing combat damage")
    void doesNotDrawForUnmodifiedCreatureCombatDamage() {
        addReadyCreature(player1, new SPDrPilotedByPeni());
        Permanent unmodifiedAttacker = addReadyCreature(player1, new GrizzlyBears());
        unmodifiedAttacker.setAttacking(true);
        harness.setHand(player1, List.of());

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
