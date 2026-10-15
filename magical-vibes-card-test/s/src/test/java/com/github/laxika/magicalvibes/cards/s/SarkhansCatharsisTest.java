package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SarkhansCatharsis.class, ElspethKnightErrant.class, GrizzlyBears.class})
class SarkhansCatharsisTest extends BaseCardTest {

    @Test
    void dealsFiveDamageToTargetPlayer() {
        cast(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    void dealsFiveDamageToTargetPlaneswalker() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 8);

        cast(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveCardAndMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsController() {
        cast(player1.getId());

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sarkhan's Catharsis");
    }

    @Test
    void canTargetItsControllersPlaneswalker() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player1, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 8);

        cast(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void putsPlaneswalkerWithFiveLoyaltyIntoGraveyard() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);

        cast(elspeth.getId());

        harness.assertNotOnBattlefield(player2, "Elspeth, Knight-Errant");
        harness.assertInGraveyard(player2, "Elspeth, Knight-Errant");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDamagePlayerWhenTargetPlaneswalkerLeavesBattlefield() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        elspeth.setCounterCount(CounterType.LOYALTY, 8);
        giveCardAndMana();
        harness.castInstant(player1, 0, elspeth.getId());
        gd.playerBattlefields.get(player2.getId()).remove(elspeth);
        gd.playerGraveyards.get(player2.getId()).add(elspeth.getCard());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sarkhan's Catharsis");
        assertThat(gd.stack).isEmpty();
    }

    private void cast(UUID targetId) {
        giveCardAndMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void giveCardAndMana() {
        harness.setHand(player1, List.of(new SarkhansCatharsis()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
