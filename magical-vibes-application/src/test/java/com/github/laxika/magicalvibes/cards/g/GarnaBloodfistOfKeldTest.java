package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarnaBloodfistOfKeld.class, GrizzlyBears.class, Shock.class, Forest.class})
class GarnaBloodfistOfKeldTest extends BaseCardTest {

    @Test
    @DisplayName("When an attacking ally dies, Garna draws a card")
    void attackingAllyDeathDrawsCard() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));

        killWithShock(attacker);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When a nonattacking ally dies, Garna deals 1 damage to each opponent")
    void nonattackingAllyDeathDamagesOpponents() {
        harness.addToBattlefield(player1, new GarnaBloodfistOfKeld());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        killWithShock(ally);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void killWithShock(Permanent target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
