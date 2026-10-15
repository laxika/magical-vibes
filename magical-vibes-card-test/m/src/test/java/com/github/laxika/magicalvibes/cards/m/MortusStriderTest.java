package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BeckonApparition;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MortusStrider.class, WrathOfGod.class, BeckonApparition.class})
class MortusStriderTest extends BaseCardTest {

    @Test
    @DisplayName("When Mortus Strider dies, it returns to its owner's hand instead of staying in the graveyard")
    void diesReturnsToOwnersHand() {
        Permanent strider = harness.addToBattlefieldAndReturn(player1, new MortusStrider());
        Card striderCard = strider.getCard();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath and put the death trigger on the stack.
        harness.passBothPriorities(); // Resolve the death trigger.

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(striderCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(striderCard.getId()));
    }

    @Test
    @DisplayName("Lethal damage puts Mortus Strider in the graveyard until its trigger resolves")
    void lethalDamageReturnsOnlyAfterTriggerResolves() {
        Card card = new MortusStrider();
        Permanent strider = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of());

        strider.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(strider);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling Mortus Strider in response prevents its return to hand")
    void exiledInResponseDoesNotReturn() {
        Card card = new MortusStrider();
        Permanent strider = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of(new BeckonApparition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        strider.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.castAndResolveInstant(player1, 0, card.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mortus Strider controlled by an opponent returns to its owner's hand")
    void returnsToOwnerRatherThanController() {
        Card card = new MortusStrider();
        Permanent strider = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(strider.getId(), player1.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        strider.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }
}
