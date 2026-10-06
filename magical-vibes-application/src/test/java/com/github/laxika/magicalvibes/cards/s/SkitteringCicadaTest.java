package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
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

@CardUsed({SkitteringCicada.class, MindStone.class, GrizzlyBears.class, WalkingBallista.class})
class SkitteringCicadaTest extends BaseCardTest {

    @Test
    void canCastColorlessSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new SkitteringCicada());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passPriority(player2);
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

        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void colorlessSpellGivesTrampleAndManaValueBoostUntilEndOfTurn() {
        harness.addToBattlefield(player1, new SkitteringCicada());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

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

    @Test
    void colorlessSpellCreatesOneTriggerThatGrantsBothBonuses() {
        Permanent cicada = harness.addToBattlefieldAndReturn(player1, new SkitteringCicada());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(cicada.getPowerModifier()).isEqualTo(2);
        assertThat(cicada.getToughnessModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void boostIncludesEachChosenXInTheSpellsManaCost() {
        Permanent cicada = harness.addToBattlefieldAndReturn(player1, new SkitteringCicada());
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(cicada.getPowerModifier()).isEqualTo(6);
        assertThat(cicada.getToughnessModifier()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void coloredSpellDoesNotTriggerEitherBonus() {
        Permanent cicada = harness.addToBattlefieldAndReturn(player1, new SkitteringCicada());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(cicada.getPowerModifier()).isZero();
        assertThat(cicada.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentsColorlessSpellDoesNotTriggerEitherBonus() {
        Permanent cicada = harness.addToBattlefieldAndReturn(player1, new SkitteringCicada());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(cicada.getPowerModifier()).isZero();
        assertThat(cicada.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canCastCicadaItselfDuringOpponentsTurnWithoutTriggeringItself() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SkitteringCicada()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent cicada = findPermanent(player1, "Skittering Cicada");
        assertThat(cicada.getPowerModifier()).isZero();
        assertThat(cicada.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, cicada, Keyword.TRAMPLE)).isFalse();
    }
}
