package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkitteringCicada.class, MindStone.class, GrizzlyBears.class})
class SkitteringCicadaTest extends BaseCardTest {

    @Test
    void canCastColorlessSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new SkitteringCicada());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.getGameService().passPriority(harness.getGameData(), player2);
        harness.castArtifact(player1, 0);

        assertThat(harness.getGameData().stack)
                .anyMatch(entry -> entry.getCard().getName().equals("Mind Stone"));
    }

    @Test
    void doesNotGiveFlashToColoredSpells() {
        harness.addToBattlefield(player1, new SkitteringCicada());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.getGameService().passPriority(harness.getGameData(), player2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void colorlessSpellGivesTrampleAndManaValueBoostUntilEndOfTurn() {
        harness.addToBattlefield(player1, new SkitteringCicada());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cicada = findPermanent(player1, "Skittering Cicada");
        assertThat(cicada.getPowerModifier()).isEqualTo(2);
        assertThat(cicada.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cicada.getPowerModifier()).isZero();
        assertThat(cicada.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isFalse();
    }
}
