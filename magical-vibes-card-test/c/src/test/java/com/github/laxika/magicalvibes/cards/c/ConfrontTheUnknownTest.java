package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExposeEvil;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
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

@CardUsed({ConfrontTheUnknown.class, ExposeEvil.class, Forest.class, QuilledWolf.class, JustTheWind.class})
class ConfrontTheUnknownTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates, then boosts target creature for each Clue controlled")
    void investigatesThenBoostsForEachControlledClue() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new ExposeEvil(), new ConfrontTheUnknown()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new ConfrontTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void ignoresOpponentsClues() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player2, List.of(new ExposeEvil()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, wolf.getId());
        harness.setHand(player1, List.of(new ConfrontTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, wolf.getId());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(wolf.getEffectivePower()).isEqualTo(3);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void countsCluesCreatedInResponseAndKeepsBoostAfterSacrifice() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new ConfrontTheUnknown(), new ExposeEvil()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, wolf.getId());
        harness.castAndResolveInstant(player1, 0, List.of());
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
        Permanent clue = findPermanents(player1, "Clue").getFirst();
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(wolf.getEffectivePower()).isEqualTo(4);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void doesNotInvestigateWhenTargetLeavesBeforeResolution() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new ConfrontTheUnknown()));
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, wolf.getId());
        harness.castAndResolveInstant(player2, 0, wolf.getId());
        harness.assertInHand(player2, "Quilled Wolf");

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInGraveyard(player1, "Confront the Unknown");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ConfrontTheUnknown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
