package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BasriKet;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent target = new Permanent(new BasriKet());
        target.setCounterCount(CounterType.LOYALTY, 3);
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);

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
