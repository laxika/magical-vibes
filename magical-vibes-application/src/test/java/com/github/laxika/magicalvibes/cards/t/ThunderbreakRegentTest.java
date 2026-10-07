package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheDragonspeaker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ThunderbreakRegent.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class, SarkhanTheDragonspeaker.class})
class ThunderbreakRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to the controller of an opponent's spell targeting a Dragon")
    void damagesOpponentSpellController() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, regent.getId());

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

        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for a spell controlled by its Dragon's controller")
    void doesNotTriggerForOwnSpell() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, regent.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Regent triggers when an opponent targets another Dragon you control")
    void eachRegentTriggersForTargetedDragon() {
        harness.addToBattlefield(player1, new ThunderbreakRegent());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Does not trigger when an opponent targets a Dragon they control")
    void doesNotTriggerForOpponentsDragon() {
        harness.addToBattlefield(player1, new ThunderbreakRegent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ThunderbreakRegent());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger for an ability controlled by its Dragon's controller")
    void doesNotTriggerForOwnAbility() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, regent.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A queued trigger still deals damage after Regent leaves the battlefield")
    void triggerResolvesAfterRegentDies() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new ThunderbreakRegent());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player2, 0, regent.getId());
        harness.castAndResolveInstant(player1, 0, regent.getId());
        harness.castAndResolveInstant(player1, 0, regent.getId());

        harness.assertInGraveyard(player1, "Thunderbreak Regent");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Triggers for Sarkhan after he becomes a Dragon creature")
    void triggersForAnimatedPlaneswalkerDragon() {
        harness.addToBattlefield(player1, new ThunderbreakRegent());
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheDragonspeaker());
        sarkhan.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sarkhan.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
