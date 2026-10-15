package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StonybrookSchoolmaster;
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

@CardUsed({CurrentCurriculum.class, CoralMerfolk.class, GrizzlyBears.class, StonybrookSchoolmaster.class})
class CurrentCurriculumTest extends BaseCardTest {

    @Test
    @DisplayName("The first Merfolk spell each turn can use convoke")
    void firstMerfolkSpellCanUseConvoke() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the first Merfolk spell each turn gets convoke")
    void onlyFirstMerfolkSpellGetsConvoke() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(
                new CoralMerfolk(),
                new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();
        convoker.untap();

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A non-Merfolk spell does not use the Merfolk allowance")
    void doesNotGrantConvokeToNonMerfolkSpell() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Conjures a tapped Stonybrook Schoolmaster with two tapped creatures")
    void conjuresTappedSchoolmasterAtEndStep() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof StonybrookSchoolmaster)
                .singleElement()
                .extracting(Permanent::isTapped)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("Does not conjure a Schoolmaster with fewer than two tapped creatures")
    void doesNotConjureWithFewerThanTwoTappedCreatures() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof StonybrookSchoolmaster);
    }

    @Test
    @DisplayName("The tapped creature condition is checked again on resolution")
    void doesNotConjureIfCreatureUntapsBeforeResolution() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        first.tap();
        second.tap();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        second.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof StonybrookSchoolmaster);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        first.tap();
        second.tap();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's tapped creatures do not satisfy the condition")
    void opponentsCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        own.tap();
        opposing.tap();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Merfolk cast before Curriculum entered still uses the allowance")
    void earlierMerfolkCastUsesAllowance() {
        harness.setHand(player1, List.of(new CoralMerfolk(), new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new CurrentCurriculum());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Merfolk leaves the first Merfolk's convoke available")
    void nonMerfolkCastDoesNotUseAllowance() {
        harness.addToBattlefield(player1, new CurrentCurriculum());
        harness.setHand(player1, List.of(new GrizzlyBears(), new CoralMerfolk()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Permanent convoker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst().orElseThrow();

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped noncreature permanents do not satisfy the condition")
    void tappedEnchantmentDoesNotCount() {
        Permanent curriculum = harness.addToBattlefieldAndReturn(player1, new CurrentCurriculum());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        curriculum.tap();
        creature.tap();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
