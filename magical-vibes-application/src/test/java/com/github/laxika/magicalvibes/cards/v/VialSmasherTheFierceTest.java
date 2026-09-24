package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VialSmasherTheFierce.class, GrizzlyBears.class, ChandraNalaar.class, NicolBolasPlaneswalker.class})
class VialSmasherTheFierceTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the first spell's mana value to the opponent")
    void dealsDamageEqualToFirstSpellManaValue() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        castGrizzlyBears();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Only the first spell each turn triggers the ability")
    void onlyFirstSpellEachTurnTriggers() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        castGrizzlyBears();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        castGrizzlyBears();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can choose the randomly selected opponent's planeswalker")
    void canChooseOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new VialSmasherTheFierce());
        Permanent planeswalker = new Permanent(new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.addToBattlefield(player2, new GrizzlyBears());
        castGrizzlyBears();

        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(planeswalker.getId());
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }
}
