package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProwlingGeistcatcher.class, BloodthroneVampire.class, GrizzlyBears.class,
        ReassemblingSkeleton.class, RestInPeace.class})
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

    @Test
    void tokenSacrificeCreatesOneGeistcatcherTrigger() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(geistcatcher.getCard().getId())))
                .hasSize(1);
        resolveAllTriggers();
        assertThat(geistcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificedCreatureExiledByReplacementIsStillLinkedToGeistcatcher() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RestInPeace());

        sacrificeWithBloodthrone(vampire, bears);

        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, geistcatcher));
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void exileTriggerDoesNotFollowCreatureThroughAnotherGraveyardEntry() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new ReassemblingSkeleton());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player1, skeleton.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returnedSkeleton = findPermanent(player1, "Reassembling Skeleton");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returnedSkeleton));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Reassembling Skeleton");
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
    }

    @Test
    void stolenCreatureReturnsUnderGeistcatcherControllersControl() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        sacrificeWithBloodthrone(vampire, bears);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, geistcatcher));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void sacrificingGeistcatcherDoesNotExileItself() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());

        sacrificeWithBloodthrone(vampire, geistcatcher);

        harness.assertInGraveyard(player1, "Prowling Geistcatcher");
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
    }

    @Test
    void creatureRemainsExiledWhenGeistcatcherLeavesBeforeExileTriggerResolves() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, geistcatcher));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
    }

    @Test
    void leavingReturnsEveryExiledCreature() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new BloodthroneVampire());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        sacrificeWithBloodthrone(vampire, first);
        sacrificeWithBloodthrone(vampire, second);
        assertThat(geistcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, geistcatcher));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
    }

    @Test
    void opponentsSacrificeDoesNotTriggerGeistcatcher() {
        Permanent geistcatcher = harness.addToBattlefieldAndReturn(player1, new ProwlingGeistcatcher());
        Permanent vampire = harness.addToBattlefieldAndReturn(player2, new BloodthroneVampire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.ensurePriority(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(vampire), null, null);
        harness.handlePermanentChosen(player2, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(geistcatcher.getId())).isEmpty();
        assertThat(geistcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
