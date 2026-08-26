package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disembowel.class, GrizzlyBears.class, HillGiant.class, Plains.class})
class DisembowelTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureWithManaValueX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        UUID target = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void cannotTargetCreatureWithDifferentManaValue() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID target = harness.getPermanentId(player2, "Hill Giant");

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        UUID target = harness.getPermanentId(player2, "Plains");

        harness.setHand(player1, List.of(new Disembowel()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, target))
                .isInstanceOf(IllegalStateException.class);
    }
}
