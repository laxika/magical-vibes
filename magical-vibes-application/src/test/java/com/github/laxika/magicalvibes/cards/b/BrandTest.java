package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brand.class, ControlMagic.class, GrizzlyBears.class})
class BrandTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of all permanents the caster owns")
    void gainsControlOfOwnedPermanents() {
        Permanent ownedByPlayer1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownedByPlayer2 = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castEnchantment(player2, 0, ownedByPlayer1.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Brand()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownedByPlayer1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ownedByPlayer1);
    }

    @Test
    @DisplayName("Cycling {2} discards Brand and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Brand()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brand");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
