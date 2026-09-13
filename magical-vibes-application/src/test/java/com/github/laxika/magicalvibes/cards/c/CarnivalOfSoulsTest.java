package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MetathranSoldier;
import com.github.laxika.magicalvibes.cards.y.YavimayaHollow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarnivalOfSouls.class, MetathranSoldier.class, YavimayaHollow.class})
class CarnivalOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under your control causes life loss and adds black mana")
    void triggersForYourCreature() {
        harness.addToBattlefield(player1, new CarnivalOfSouls());
        harness.setHand(player1, List.of(new MetathranSoldier()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature also causes the Carnival controller's trigger")
    void triggersForOpponentsCreature() {
        harness.addToBattlefield(player1, new CarnivalOfSouls());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new MetathranSoldier()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A noncreature permanent entering does not trigger Carnival of Souls")
    void doesNotTriggerForNonCreature() {
        harness.addToBattlefield(player1, new CarnivalOfSouls());
        harness.setHand(player1, List.of(new YavimayaHollow()));

        harness.playLand(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
