package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LuminousRebuke;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieFencing.class, GrizzlyBears.class, FaerieDreamthief.class, LuminousRebuke.class})
class FaerieFencingTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X without the Faerie bonus")
    void appliesBaseMinusXMinusX() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Applies the Faerie bonus based on the battlefield at cast time")
    void appliesFaerieBonusEvenIfFaerieLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new FaerieDreamthief());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaerieFencing()));
        harness.setHand(player2, List.of(new LuminousRebuke()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 5);

        var faerieId = harness.getPermanentId(player1, "Faerie Dreamthief");
        var bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 1, bearId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, faerieId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Faerie Dreamthief");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
