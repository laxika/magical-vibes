package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Themberchaud.class, Mountain.class, ColossalDreadmaw.class, AirElemental.class})
class ThemberchaudTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and deals damage equal to your Mountains to other nonflying creatures and each player")
    void entersAndDealsMountainDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        Permanent ownGroundCreature = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent opposingGroundCreature = addCreatureReady(player2, new ColossalDreadmaw());
        Permanent opposingFlyer = addCreatureReady(player2, new AirElemental());

        harness.setHand(player1, List.of(new Themberchaud()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        Permanent themberchaud = findPermanent(player1, "Themberchaud");
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(themberchaud.getMarkedDamage()).isZero();
        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyer.getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exerting as it attacks gives flying and skips the next untap")
    void exertingGivesFlyingAndSkipsUntap() {
        Permanent themberchaud = addCreatureReady(player1, new Themberchaud());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isTrue();
        assertThat(themberchaud.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert does not give flying or skip the next untap")
    void decliningExertDoesNothing() {
        Permanent themberchaud = addCreatureReady(player1, new Themberchaud());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, themberchaud, Keyword.FLYING)).isFalse();
        assertThat(themberchaud.getSkipUntapCount()).isZero();
    }
}
