package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonoredDreyleader.class})
class HonoredDreyleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each other Squirrel and Food controlled")
    void entersWithCountersForOtherSquirrelsAndFood() {
        addToken(player1, "Squirrel", CardType.CREATURE, CardSubtype.SQUIRREL);
        addToken(player1, "Food", CardType.ARTIFACT, CardSubtype.FOOD);
        harness.castFromHand(player1, new HonoredDreyleader(), "{2}{G}");
        resolveAllTriggers();

        Permanent dreyleader = gd.playerBattlefields.get(player1.getId()).getLast();

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a counter when another controlled Squirrel or Food enters")
    void getsCounterWhenAnotherSquirrelOrFoodEnters() {
        Permanent dreyleader = harness.addToBattlefieldAndReturn(player1, new HonoredDreyleader());

        Permanent squirrel = addToken(player1, "Squirrel", CardType.CREATURE, CardSubtype.SQUIRREL);
        triggerPermanentEntry(player1, squirrel);
        Permanent food = addToken(player1, "Food", CardType.ARTIFACT, CardSubtype.FOOD);
        triggerPermanentEntry(player1, food);

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Squirrel or a nonmatching permanent")
    void doesNotTriggerForOpponentSquirrelOrNonmatchingPermanent() {
        Permanent dreyleader = harness.addToBattlefieldAndReturn(player1, new HonoredDreyleader());

        Permanent opponentSquirrel = addToken(player2, "Squirrel", CardType.CREATURE, CardSubtype.SQUIRREL);
        triggerPermanentEntry(player2, opponentSquirrel);
        Permanent nonmatching = addToken(player1, "Bear", CardType.CREATURE, CardSubtype.BEAR);
        triggerPermanentEntry(player1, nonmatching);

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count itself or opponents' permanents on entry")
    void excludesItselfAndOpponentsPermanents() {
        harness.addToBattlefield(player2, new HonoredDreyleader());
        addToken(player2, "Food", CardType.ARTIFACT, CardSubtype.FOOD);

        Permanent dreyleader = harness.enterBattlefieldAndReturn(player1, new HonoredDreyleader());
        resolveAllTriggers();

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another Dreyleader receives its entry counters and triggers the existing one")
    void anotherDreyleaderTriggersExistingDreyleader() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HonoredDreyleader());

        Permanent second = harness.enterBattlefieldAndReturn(player1, new HonoredDreyleader());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entry counts permanents remaining when the ability resolves")
    void entryCountsPermanentsAtResolution() {
        Permanent squirrel = harness.addToBattlefieldAndReturn(player1, new HonoredDreyleader());
        Permanent dreyleader = harness.enterBattlefieldAndReturn(player1, new HonoredDreyleader());
        gd.playerBattlefields.get(player1.getId()).remove(squirrel);
        gd.playerGraveyards.get(player1.getId()).add(squirrel.getCard());

        resolveAllTriggers();

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A permanent that is both Squirrel and Food is counted only once")
    void countsSquirrelFoodOnce() {
        addToken(player1, "Squirrel Food", CardType.CREATURE, CardSubtype.SQUIRREL, CardSubtype.FOOD);
        Permanent dreyleader = harness.enterBattlefieldAndReturn(player1, new HonoredDreyleader());
        resolveAllTriggers();

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent anotherSquirrelFood = addToken(player1, "Squirrel Food", CardType.CREATURE,
                CardSubtype.SQUIRREL, CardSubtype.FOOD);
        triggerPermanentEntry(player1, anotherSquirrelFood);

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter trigger still resolves after the entering Squirrel leaves")
    void counterTriggerSurvivesEnteringSquirrelLeaving() {
        Permanent dreyleader = harness.addToBattlefieldAndReturn(player1, new HonoredDreyleader());
        Permanent squirrel = harness.enterBattlefieldAndReturn(player1, new HonoredDreyleader());
        gd.playerBattlefields.get(player1.getId()).remove(squirrel);
        gd.playerGraveyards.get(player1.getId()).add(squirrel.getCard());

        resolveAllTriggers();

        assertThat(dreyleader.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An entry ability cannot put counters on a new permanent represented by the same card")
    void entryTriggerDoesNotFollowSourceReturning() {
        harness.addToBattlefield(player1, new HonoredDreyleader());
        HonoredDreyleader card = new HonoredDreyleader();
        Permanent original = harness.enterBattlefieldAndReturn(player1, card);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, card);

        resolveAllTriggers();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addToken(Player player, String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        if (type == CardType.CREATURE && List.of(subtypes).contains(CardSubtype.FOOD)) {
            card.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        }
        card.setToken(true);
        if (type == CardType.CREATURE) {
            card.setPower(1);
            card.setToughness(1);
        }

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void triggerPermanentEntry(Player controller, Permanent enteringPermanent) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAnyPermanentEntersTriggers(gd, controller.getId(), enteringPermanent.getCard()));
        resolveAllTriggers();
    }
}
