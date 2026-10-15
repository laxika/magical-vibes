package com.github.laxika.magicalvibes.cards.v;

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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValleyMightcaller.class})
class ValleyMightcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a counter for each matching creature that enters under your control")
    void getsCounterForEachMatchingCreatureType() {
        Permanent mightcaller = harness.addToBattlefieldAndReturn(player1, new ValleyMightcaller());

        triggerPermanentEntry(player1, addToken(player1, "Frog", CardSubtype.FROG));
        triggerPermanentEntry(player1, addToken(player1, "Rabbit", CardSubtype.RABBIT));
        triggerPermanentEntry(player1, addToken(player1, "Raccoon", CardSubtype.RACCOON));
        triggerPermanentEntry(player1, addToken(player1, "Squirrel", CardSubtype.SQUIRREL));

        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's matching creature or a nonmatching creature")
    void doesNotTriggerForOpponentOrNonmatchingCreature() {
        Permanent mightcaller = harness.addToBattlefieldAndReturn(player1, new ValleyMightcaller());

        triggerPermanentEntry(player2, addToken(player2, "Frog", CardSubtype.FROG));
        triggerPermanentEntry(player1, addToken(player1, "Bear", CardSubtype.BEAR));

        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when Valley Mightcaller itself enters")
    void doesNotTriggerForItsOwnEntry() {
        harness.castFromHand(player1, new ValleyMightcaller(), "{G}");
        harness.passBothPriorities();

        Permanent mightcaller = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Valley Mightcaller triggers the existing one but not itself")
    void anotherMightcallerTriggersOnlyExistingMightcaller() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ValleyMightcaller());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new ValleyMightcaller());

        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature with multiple matching subtypes gives only one counter")
    void multipleMatchingSubtypesGiveOnlyOneCounter() {
        Permanent mightcaller = harness.addToBattlefieldAndReturn(player1, new ValleyMightcaller());
        Permanent token = addToken(player1, "Frog Rabbit", CardSubtype.FROG, CardSubtype.RABBIT);

        triggerPermanentEntry(player1, token);

        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter is still placed after the entering creature leaves")
    void enteringCreatureLeavingDoesNotPreventCounter() {
        Permanent mightcaller = harness.addToBattlefieldAndReturn(player1, new ValleyMightcaller());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new ValleyMightcaller());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(entering);
        gd.playerGraveyards.get(player1.getId()).add(entering.getCard());

        resolveAllTriggers();

        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addToken(Player player, String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtypes));
        card.setToken(true);
        card.setPower(1);
        card.setToughness(1);

        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void triggerPermanentEntry(Player controller, Permanent enteringPermanent) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyCreatureEntersTriggers(gd, controller.getId(), enteringPermanent.getCard(), 0));
        resolveAllTriggers();
    }
}
