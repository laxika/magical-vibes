package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SarkhanFireblood;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViashinoPyromancer.class, SarkhanFireblood.class})
class ViashinoPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to target player")
    void etbDealsTwoDamageToTargetPlayer() {
        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("ETB deals 2 damage to target planeswalker")
    void etbDealsTwoDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanFireblood());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void etbCanDamageController() {
        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ViashinoPyromancer());
        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB still resolves after its source leaves the battlefield")
    void etbResolvesWithoutSource() {
        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        Permanent source = findPermanent(player1, "Viashino Pyromancer");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("ETB can kill its controller's planeswalker")
    void etbCanKillOwnPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new SarkhanFireblood());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new ViashinoPyromancer()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sarkhan, Fireblood");
        harness.assertInGraveyard(player1, "Sarkhan, Fireblood");
        harness.assertLife(player1, 20);
    }
}
