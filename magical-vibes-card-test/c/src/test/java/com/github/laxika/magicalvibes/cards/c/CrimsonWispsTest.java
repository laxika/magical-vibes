package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BallynockCohort;
import com.github.laxika.magicalvibes.cards.i.IlluminatedFolio;
import com.github.laxika.magicalvibes.cards.t.TurnToMist;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrimsonWisps.class, BallynockCohort.class, IlluminatedFolio.class, TurnToMist.class})
class CrimsonWispsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving makes target creature red until end of turn")
    void resolvingMakesTargetRed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        // "Becomes red" replaces the colors (CR 105.3), applied by the CR 613 layer engine.
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Resolving grants target creature haste until end of turn")
    void resolvingGrantsHaste() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BallynockCohort());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Color and haste wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.RED);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new BallynockCohort());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new IlluminatedFolio());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not draw when the sole target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BallynockCohort());
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.setHand(player2, List.of(new TurnToMist()));
        harness.setLibrary(player1, List.of(new IlluminatedFolio()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Crimson Wisps");
    }
}
