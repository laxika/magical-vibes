package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed(HonoredDreyleader.class)
class HonoredDreyleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each other Squirrel and Food controlled")
    void entersWithCountersForOtherSquirrelsAndFood() {
        addToken(player1, "Squirrel", CardType.CREATURE, CardSubtype.SQUIRREL);
        addToken(player1, "Food", CardType.ARTIFACT, CardSubtype.FOOD);
        harness.setHand(player1, List.of(new HonoredDreyleader()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private Permanent addToken(Player player, String name, CardType type, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtype));
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
        resolveAllStackEntries();
    }

    private void resolveAllStackEntries() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
