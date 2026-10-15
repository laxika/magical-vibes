package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DisciplinedDuelist;
import com.github.laxika.magicalvibes.cards.e.ElegantEntourage;
import com.github.laxika.magicalvibes.cards.h.HaloFountain;
import com.github.laxika.magicalvibes.cards.j.JewelThief;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncandescentAria.class, ElegantEntourage.class, HaloFountain.class, JewelThief.class,
        DisciplinedDuelist.class})
class IncandescentAriaTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each nontoken creature on both battlefields")
    void damagesEachNontokenCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ElegantEntourage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new ElegantEntourage());

        castIncandescentAria();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not damage creature tokens")
    void doesNotDamageCreatureTokens() {
        Permanent ownToken = addTokenCreature(player1);
        Permanent opponentToken = addTokenCreature(player2);

        castIncandescentAria();

        assertThat(ownToken.getMarkedDamage()).isZero();
        assertThat(opponentToken.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownToken);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentToken);
    }

    @Test
    @DisplayName("Lethal damage kills nontoken creatures while tokens and noncreatures are untouched")
    void killsNontokenCreaturesWithoutHarmingOtherPermanentsOrPlayers() {
        harness.addToBattlefield(player1, new JewelThief());
        harness.addToBattlefield(player2, new JewelThief());
        Permanent ownToken = addTokenCreature(player1);
        Permanent opponentToken = addTokenCreature(player2);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HaloFountain());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new HaloFountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castIncandescentAria();

        harness.assertInGraveyard(player1, "Jewel Thief");
        harness.assertInGraveyard(player2, "Jewel Thief");
        harness.assertNotOnBattlefield(player1, "Jewel Thief");
        harness.assertNotOnBattlefield(player2, "Jewel Thief");
        assertThat(ownToken.getMarkedDamage()).isZero();
        assertThat(opponentToken.getMarkedDamage()).isZero();
        assertThat(ownArtifact.getMarkedDamage()).isZero();
        assertThat(opponentArtifact.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownToken, ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentToken, opponentArtifact);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolves on an empty battlefield without damaging either player")
    void resolvesWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castIncandescentAria();

        harness.assertInGraveyard(player1, "Incandescent Aria");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A shield counter prevents Aria's damage to a nontoken creature")
    void shieldCounterPreventsDamage() {
        Permanent duelist = harness.enterBattlefieldAndReturn(player2, new DisciplinedDuelist());

        castIncandescentAria();

        assertThat(duelist.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(duelist.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Disciplined Duelist");
        harness.assertNotInGraveyard(player2, "Disciplined Duelist");
    }

    private void castIncandescentAria() {
        harness.castFromHand(player1, new IncandescentAria(), "{R}{G}{W}");
        harness.passBothPriorities();
    }

    private Permanent addTokenCreature(Player player) {
        ElegantEntourage card = new ElegantEntourage();
        card.setToken(true);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
