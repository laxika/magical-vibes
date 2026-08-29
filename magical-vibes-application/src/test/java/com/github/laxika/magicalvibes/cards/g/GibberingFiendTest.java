package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class GibberingFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and deals 1 damage to each opponent")
    void dealsDamageToEachOpponentOnEnter() {
        harness.setHand(player1, List.of(new GibberingFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Delirium deals 1 damage to an opponent during that opponent's upkeep")
    void dealsDamageOnOpponentsUpkeepWithDelirium() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not deal damage during an opponent's upkeep without delirium")
    void doesNotDealDamageWithoutDelirium() {
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger during its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Rechecks delirium when the triggered ability resolves")
    void rechecksDeliriumAtResolution() {
        setDelirium();
        harness.addToBattlefield(player1, new GibberingFiend());

        advanceToUpkeep(player2);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
