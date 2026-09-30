package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwlingGeistcatcher.class, BloodthroneVampire.class, GrizzlyBears.class})
class ProwlingGeistcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature exiles it with Prowling Geistcatcher")
    void sacrificingCreatureExilesItWithSource() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeWithBloodthrone(vampire, bears);

        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    @DisplayName("Sacrificing a creature token puts a +1/+1 counter on Prowling Geistcatcher")
    void sacrificingTokenPutsCounterOnSource() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());

        sacrificeWithBloodthrone(vampire, token);

        assertThat(geistcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Prowling Geistcatcher leaves, exiled cards return under your control")
    void leavingReturnsExiledCardsUnderYourControl() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeWithBloodthrone(vampire, bears);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, geistcatcher));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
    }

    private void sacrificeWithBloodthrone(Permanent vampire, Permanent sacrificed) {
        int vampireIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vampire);
        harness.activateAbility(player1, vampireIndex, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        resolveAllTriggers();
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setName("Spirit Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
