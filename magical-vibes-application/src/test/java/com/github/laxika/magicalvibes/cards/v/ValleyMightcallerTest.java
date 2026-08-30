package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed(ValleyMightcaller.class)
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
        harness.setHand(player1, List.of(new ValleyMightcaller()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mightcaller = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mightcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addToken(Player player, String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setToken(true);
        card.setPower(1);
        card.setToughness(1);

        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void triggerPermanentEntry(Player controller, Permanent enteringPermanent) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyCreatureEntersTriggers(gd, controller.getId(), enteringPermanent.getCard(), 0));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
