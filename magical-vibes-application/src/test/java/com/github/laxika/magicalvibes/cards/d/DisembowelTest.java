package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenevolentAncestor;
import com.github.laxika.magicalvibes.cards.h.HuntedLammasu;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disembowel.class, Watchwolf.class, BenevolentAncestor.class, Plains.class, HuntedLammasu.class})
class DisembowelTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureWithManaValueX() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Watchwolf()).getId();
        harness.addToBattlefield(player2, new BenevolentAncestor());

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertOnBattlefield(player2, "Benevolent Ancestor");
    }

    @Test
    void cannotTargetCreatureWithDifferentManaValue() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new BenevolentAncestor()).getId();

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLand() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Plains()).getId();

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysZeroManaValueCreatureTokenWhenXIsZero() {
        harness.setHand(player1, List.of(new HuntedLammasu()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID token = findPermanent(player2, "Horror").getId();
        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, 0, token);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Horror");
    }

    @Test
    void canDestroyItsControllersCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new Watchwolf()).getId();

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Disembowel");
    }

    @Test
    void cannotCastWithoutPayingXAndBlackMana() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Watchwolf()).getId();

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Disembowel");
        harness.assertOnBattlefield(player2, "Watchwolf");
    }

    @Test
    void doesNotDestroyAnotherCreatureWhenTargetLeavesBeforeResolution() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new Watchwolf()).getId();
        harness.addToBattlefield(player2, new BenevolentAncestor());

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, target);

        harness.setHand(player2, List.of(new Disembowel()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, 2, target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Watchwolf");
        harness.assertOnBattlefield(player2, "Benevolent Ancestor");
        harness.assertInGraveyard(player1, "Disembowel");
        harness.assertInGraveyard(player2, "Disembowel");
    }
}
