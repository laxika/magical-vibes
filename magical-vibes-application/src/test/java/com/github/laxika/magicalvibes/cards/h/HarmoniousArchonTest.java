package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArchonOfJustice;
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

@CardUsed({HarmoniousArchon.class, ArchonOfJustice.class, GrizzlyBears.class})
class HarmoniousArchonTest extends BaseCardTest {

    @Test
    @DisplayName("Non-Archon creatures on the battlefield have base 3/3")
    void setsNonArchonCreaturesToThreeThree() {
        addCreatureReady(player1, new HarmoniousArchon());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingArchon = addCreatureReady(player2, new ArchonOfJustice());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingArchon)).isNotEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingArchon)).isNotEqualTo(3);
    }

    @Test
    @DisplayName("Entering Harmonious Archon creates two Human tokens")
    void createsTwoHumanTokens() {
        harness.setHand(player1, List.of(new HarmoniousArchon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Human");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        });
    }
}
