package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.r.RisenReef;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.cards.v.VoraciousHydra;
import com.github.laxika.magicalvibes.cards.z.ZoZuThePunisher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YarokTheDesecrated.class, ElvishVisionary.class, Forest.class,
        TatyovaBenthicDruid.class, ZoZuThePunisher.class, CloudkinSeer.class, RisenReef.class,
        AirElemental.class, GreenwoodSentinel.class, VoraciousHydra.class})
class YarokTheDesecratedTest extends BaseCardTest {

    @Test
    @DisplayName("Yarok doubles a creature's enter-the-battlefield ability")
    void doublesCreatureEnterAbility() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());

        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Yarok doubles a landfall ability when a land enters")
    void doublesLandfallAbility() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Yarok doubles a permanent's trigger when an opponent's permanent enters")
    void doublesTriggerFromOpponentPermanentEntering() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Yarok does not double an opponent's triggered ability")
    void doesNotDoubleOpponentTrigger() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        harness.addToBattlefield(player2, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both instances of a doubled draw trigger resolve independently")
    void resolvesBothDrawTriggers() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        CloudkinSeer firstCard = new CloudkinSeer();
        CloudkinSeer secondCard = new CloudkinSeer();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.castFromHand(player1, new CloudkinSeer(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Yarok doubles Risen Reef's trigger caused by Yarok's own entry")
    void doublesTriggerCausedByItsOwnEntry() {
        harness.addToBattlefield(player1, new RisenReef());
        CloudkinSeer firstCard = new CloudkinSeer();
        CloudkinSeer secondCard = new CloudkinSeer();
        harness.setLibrary(player1, List.of(firstCard, secondCard));

        harness.castFromHand(player1, new YarokTheDesecrated(), "{2}{B}{G}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    @DisplayName("Yarok does not double an opponent's creature's own entry trigger")
    void doesNotDoubleOpponentCreatureEntry() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        CloudkinSeer drawnCard = new CloudkinSeer();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new CloudkinSeer(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Doubled modal entry abilities allow different modes for each instance")
    void choosesModesSeparatelyForAdditionalTrigger() {
        harness.addToBattlefield(player1, new YarokTheDesecrated());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        VoraciousHydra hydraCard = new VoraciousHydra();
        harness.setHand(player1, List.of(hydraCard));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Voracious Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.handleListChoice(player1, "Double the number of +1/+1 counters on this creature");
        harness.handleListChoice(player1, "This creature fights target creature you don't control");
        harness.handlePermanentChosen(player1, opponent.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        harness.assertOnBattlefield(player1, "Voracious Hydra");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Yarok kills a larger blocker with deathtouch and gains life from lifelink")
    void combatDamageAppliesDeathtouchAndLifelink() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new YarokTheDesecrated());
        addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Yarok, the Desecrated");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertLife(player1, 23);
    }
}
