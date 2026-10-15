package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraDressedToKill;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.cards.w.WeddingInvitation;
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

@CardUsed({EndTheFestivities.class, GrizzlyBears.class, ChandraDressedToKill.class, SnarlingWolf.class, WeddingInvitation.class})
class EndTheFestivitiesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent and their creatures and planeswalkers")
    void damagesEachOpponentAndTheirCreaturesAndPlaneswalkers() {
        Permanent ownCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreature(player2);
        Permanent ownPlaneswalker = addPlaneswalker(player1, 3);
        Permanent opponentPlaneswalker = addPlaneswalker(player2, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damages the opponent even when no permanents are on the battlefield")
    void damagesOpponentOnEmptyBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "End the Festivities");
    }

    @Test
    @DisplayName("Kills opposing one-toughness creatures and one-loyalty planeswalkers but spares artifacts")
    void killsFragileOpposingPermanentsAndSparesArtifacts() {
        Permanent ownWolf = harness.addToBattlefieldAndReturn(player1, new SnarlingWolf());
        harness.addToBattlefield(player2, new SnarlingWolf());
        Permanent ownPlaneswalker = addPlaneswalker(player1, 1);
        addPlaneswalker(player2, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new WeddingInvitation());
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player2, "Snarling Wolf");
        harness.assertInGraveyard(player2, "Chandra, Dressed to Kill");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownWolf, ownPlaneswalker);
        assertThat(ownWolf.getMarkedDamage()).isZero();
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(artifact.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Uses creature control at resolution rather than ownership or control when cast")
    void usesCreatureControlAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EndTheFestivities()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraDressedToKill());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
