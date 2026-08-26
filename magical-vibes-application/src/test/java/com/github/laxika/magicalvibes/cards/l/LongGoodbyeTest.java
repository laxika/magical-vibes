package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongGoodbye.class, Cancel.class, GrizzlyBears.class, HillGiant.class, JaceBeleren.class})
class LongGoodbyeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with mana value 3 or less")
    void destroysSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLongGoodbye(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys a target planeswalker with mana value 3 or less")
    void destroysSmallPlaneswalker() {
        Permanent target = new Permanent(new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(target);

        castLongGoodbye(target);

        harness.assertNotOnBattlefield(player2, "Jace Beleren");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void rejectsLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new LongGoodbye()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LongGoodbye longGoodbye = new LongGoodbye();
        Cancel cancel = new Cancel();
        harness.setHand(player1, List.of(longGoodbye));
        addCastingMana();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, longGoodbye.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void castLongGoodbye(Permanent target) {
        harness.setHand(player1, List.of(new LongGoodbye()));
        addCastingMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
