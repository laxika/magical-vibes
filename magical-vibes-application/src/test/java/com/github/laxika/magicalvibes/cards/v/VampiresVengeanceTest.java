package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampiresVengeance.class, CaptivatingVampire.class, GiantSpider.class})
class VampiresVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each non-Vampire creature and creates a Blood token")
    void damagesNonVampireCreaturesAndCreatesBlood() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent nonVampire = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.castFromHand(player1, new VampiresVengeance(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(vampire.getMarkedDamage()).isZero();
        assertThat(nonVampire.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Damages both players' non-Vampires while sparing both players' Vampires")
    void affectsBothBattlefieldsWithoutDamagingPlayers() {
        Permanent ownSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent ownVampire = harness.addToBattlefieldAndReturn(player1, new CaptivatingVampire());
        Permanent opposingVampire = harness.addToBattlefieldAndReturn(player2, new CaptivatingVampire());
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opposingLife = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new VampiresVengeance(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(ownSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(ownVampire.getMarkedDamage()).isZero();
        assertThat(opposingVampire.getMarkedDamage()).isZero();
        harness.assertLife(player1, ownLife);
        harness.assertLife(player2, opposingLife);
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    @DisplayName("Lethal damage kills non-Vampires on both sides and still creates Blood")
    void lethalDamageDoesNotPreventBloodCreation() {
        Permanent ownSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        ownSpider.setMarkedDamage(2);
        opposingSpider.setMarkedDamage(2);

        harness.castFromHand(player1, new VampiresVengeance(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSpider.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSpider.getCard());
        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Creates usable Blood even when there are no creatures")
    void emptyBattlefieldStillCreatesBloodWithDiscardAndDrawAbility() {
        harness.castFromHand(player1, new VampiresVengeance(), "{2}{R}");
        harness.passBothPriorities();
        Permanent blood = findPermanent(player1, "Blood");
        VampiresVengeance discarded = new VampiresVengeance();
        VampiresVengeance drawn = new VampiresVengeance();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
