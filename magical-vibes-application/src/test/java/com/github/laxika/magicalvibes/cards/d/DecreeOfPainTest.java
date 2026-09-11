package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.ManorGargoyle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfPain.class, GrizzlyBears.class, HillGiant.class, ManorGargoyle.class})
class DecreeOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and draws one card for each creature actually destroyed")
    void destroysAllCreaturesAndDrawsForEachDestroyed() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new ManorGargoyle());
        harness.setLibrary(player1, List.of(new HillGiant(), new HillGiant()));
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Manor Gargoyle");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cycling gives all creatures -2/-2 until end of turn and draws a card")
    void cyclingDebuffsAllCreaturesAndDraws() {
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DecreeOfPain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Decree of Pain");
        harness.assertInHand(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGiant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingGiant)).isEqualTo(3);
    }
}
