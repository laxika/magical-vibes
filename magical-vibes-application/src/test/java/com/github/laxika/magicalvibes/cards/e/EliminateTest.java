package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BasriKet;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TeferiMasterOfTime;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eliminate.class, BasriKet.class, EnormousBaloth.class, Forest.class, GrizzlyBears.class,
        TeferiMasterOfTime.class, Unsubstantiate.class})
class EliminateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with mana value 3 or less")
    void destroysLowManaValueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a planeswalker with mana value 3 or less")
    void destroysLowManaValuePlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BasriKet());
        target.setCounterCount(CounterType.LOYALTY, 3);

        cast(target);

        harness.assertNotOnBattlefield(player2, "Basri Ket");
        harness.assertInGraveyard(player2, "Basri Ket");
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than 3")
    void cannotTargetHighManaValuePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EnormousBaloth());
        harness.setHand(player1, List.of(new Eliminate()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature nonplaneswalker")
    void cannotTargetNoncreatureNonplaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Eliminate()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a planeswalker with mana value greater than 3")
    void cannotTargetHighManaValuePlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TeferiMasterOfTime());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Eliminate()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a creature returned to hand in response")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Eliminate()));
        harness.setHand(player2, List.of(new Unsubstantiate()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Eliminate");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new Eliminate()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
