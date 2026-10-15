package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KnightOfDusksShadow;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImposeHierarchy.class, EnormousBaloth.class, Forest.class, GrizzlyBears.class, Shock.class,
        HillGiant.class, KnightOfDusksShadow.class})
class ImposeHierarchyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with mana value 4 or less and gains life for its controller's creature cards in hand")
    void destroysCreatureAndGainsLifeForCreatureCardsInTargetControllerHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImposeHierarchy(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 4")
    void cannotTargetHighManaValueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void destroysCreatureWithManaValueExactlyFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void canDestroyOwnCreatureAndCountsOwnHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImposeHierarchy(), new GrizzlyBears(), new Shock()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    void stillDestroysWhenControllerHasNoCreatureCardsInHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Forest()));
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    void gainsNoLifeWhenTargetIsRemovedInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Impose Hierarchy");
        harness.assertLife(player1, 20);
    }

    @Test
    void destroysLifeGainPreventingCreatureBeforeGainingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnightOfDusksShadow());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ImposeHierarchy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Knight of Dusk's Shadow");
        harness.assertLife(player1, 22);
    }
}
