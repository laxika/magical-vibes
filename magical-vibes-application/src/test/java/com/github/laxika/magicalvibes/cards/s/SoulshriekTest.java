package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FesteringGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulshriekTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +X/+0 where X is the number of creature cards in your graveyard")
    void boostsByCreatureCardsInGraveyard() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new FesteringGoblin()));

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Gives no boost with an empty graveyard")
    void noBoostWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        int basePower = gqs.getEffectivePower(gd, bears);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gqs.findPermanentById(gd, bears.getId()))).isEqualTo(basePower);
    }

    @Test
    @DisplayName("The boosted creature is sacrificed at the beginning of the next end step")
    void sacrificesTargetAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.setHand(player1, List.of(new Soulshriek()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
