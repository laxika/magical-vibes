package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TearsOfValakut.class, Cancel.class, GrizzlyBears.class, SuntailHawk.class})
class TearsOfValakutTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to target creature with flying")
    void dealsFiveDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        cast(target);

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TearsOfValakut()));
        addTearsMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        TearsOfValakut tears = new TearsOfValakut();
        harness.setHand(player1, List.of(tears));
        addTearsMana();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, tears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tears of Valakut");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TearsOfValakut()));
        addTearsMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addTearsMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
