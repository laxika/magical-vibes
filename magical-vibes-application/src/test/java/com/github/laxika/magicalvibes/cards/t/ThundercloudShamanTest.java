package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.s.SentinelsOfGlenElendra;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({Card.class, ThundercloudShaman.class, AvianChangeling.class, BlindSpotGiant.class,
        ChandraNalaar.class, GoldmeadowHarrier.class, SentinelsOfGlenElendra.class})
class ThundercloudShamanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage equal to the number of Giants you control (including itself) to each non-Giant creature")
    void etbDealsDamageEqualToGiantCount() {
        // player1 already controls another Giant; the Shaman itself makes two Giants total.
        harness.addToBattlefield(player1, makeCreature("Giant Ally", 4, 4, CardSubtype.GIANT));
        Permanent target = harness.addToBattlefieldAndReturn(player2, makeCreature("Grizzly Bears", 2, 3));

        castShaman(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not damage Giant creatures")
    void etbDoesNotDamageGiants() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, makeCreature("Hill Giant", 3, 3, CardSubtype.GIANT));

        castShaman(player1);

        assertThat(giant.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB destroys non-Giant creatures with toughness at or below the damage")
    void etbKillsSmallNonGiants() {
        // Only the Shaman itself is a Giant -> 1 damage.
        harness.addToBattlefield(player2, makeCreature("Goblin", 1, 1));

        castShaman(player1);

        harness.assertNotOnBattlefield(player2, "Goblin");
        harness.assertInGraveyard(player2, "Goblin");
    }

    @Test
    void doesNotDamageNoncreaturePlaneswalkers() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        castShaman(player1);

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void changelingCountsAsGiantAndIsNotDamaged() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new BlindSpotGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SentinelsOfGlenElendra());

        castShaman(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(changeling.getMarkedDamage()).isZero();
        assertThat(opponentGiant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Avian Changeling");
    }

    @Test
    void damagesOwnNonGiantCreaturesWithoutDamagingPlayers() {
        harness.addToBattlefield(player1, new GoldmeadowHarrier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SentinelsOfGlenElendra());
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        castShaman(player1);

        harness.assertInGraveyard(player1, "Goldmeadow Harrier");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void countsGiantsAtResolutionRatherThanWhenTriggerIsCreated() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SentinelsOfGlenElendra());
        harness.setHand(player1, List.of(new ThundercloudShaman()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new BlindSpotGiant());

        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private void castShaman(Player player) {
        harness.setHand(player, List.of(new ThundercloudShaman()));
        harness.addMana(player, ManaColor.RED, 5);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }

    private Card makeCreature(String name, int power, int toughness, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }
}
