package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ThunderbreakRegent.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class})
class ThunderbreakRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the controller of an opponent's spell targeting a Dragon")
    void damagesOpponentSpellController() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, regent.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals 3 damage to the controller of an opponent's ability targeting a Dragon")
    void damagesOpponentAbilityController() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, regent.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not trigger when an opponent targets a non-Dragon creature")
    void doesNotTriggerForNonDragon() {
        harness.addToBattlefield(player1, new ThunderbreakRegent());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for a spell controlled by its Dragon's controller")
    void doesNotTriggerForOwnSpell() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, regent.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
