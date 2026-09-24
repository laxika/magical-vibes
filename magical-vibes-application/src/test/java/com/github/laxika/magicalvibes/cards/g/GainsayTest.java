package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DaringLeap;
import com.github.laxika.magicalvibes.cards.m.MoggJailer;
import com.github.laxika.magicalvibes.cards.s.StormscapeFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gainsay.class, StormscapeFamiliar.class, MoggJailer.class, DaringLeap.class})
class GainsayTest extends BaseCardTest {

    @Test
    void countersBlueSpell() {
        StormscapeFamiliar familiar = new StormscapeFamiliar();
        harness.castFromHand(player1, familiar, "{1}{U}");
        harness.setHand(player2, List.of(new Gainsay()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, familiar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Stormscape Familiar");
        harness.assertNotOnBattlefield(player1, "Stormscape Familiar");
    }

    @Test
    void countersMulticoloredBlueInstantSpell() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar());

        DaringLeap daringLeap = new DaringLeap();
        harness.setHand(player1, List.of(daringLeap));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, familiar.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Gainsay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, daringLeap.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daring Leap");
        harness.assertInGraveyard(player2, "Gainsay");
    }

    @Test
    void cannotTargetNonBlueSpell() {
        MoggJailer jailer = new MoggJailer();
        harness.castFromHand(player1, jailer, "{1}{R}");

        harness.setHand(player2, List.of(new Gainsay()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, jailer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
