package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThassaDeepDwelling.class, RiptideTurtle.class, NyxbornCourser.class})
class ThassaDeepDwellingTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa is not a creature below five blue devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent thassa = addThassa();

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa becomes a creature at five blue devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(4);

        assertThat(gqs.isCreature(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("The end-step ability immediately flickers another creature you control")
    void flickersAnotherCreatureAtEndStep() {
        addThassa();
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, courser.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(courser.getId()));
        assertThat(findPermanents(player1, "Nyxborn Courser")).hasSize(1);
    }

    @Test
    @DisplayName("The end-step ability can resolve without choosing a creature")
    void endStepAbilityCanChooseNoCreature() {
        addThassa();
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());

        advanceToEndStep();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courser);
    }

    @Test
    @DisplayName("The activated ability taps another target creature")
    void tapsAnotherCreature() {
        addThassa();
        Permanent courser = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, courser.getId());
        harness.passBothPriorities();

        assertThat(courser.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target Thassa itself")
    void cannotTargetItself() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thassa.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    @Test
    @DisplayName("Thassa stops being a creature when blue devotion falls below five")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        assertThat(gqs.isCreature(gd, thassa)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(1);

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("An opponent's blue permanents do not contribute to Thassa's devotion")
    void opponentPermanentsDoNotContributeDevotion() {
        Permanent thassa = addThassa();
        addBluePermanents(3);
        harness.addToBattlefield(player2, new RiptideTurtle());

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
    }

    @Test
    @DisplayName("Thassa does not trigger during an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        addThassa();
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(courser);
    }

    @Test
    @DisplayName("A stolen creature returns under Thassa's controller rather than its owner")
    void returnsStolenCreatureUnderYourControl() {
        addThassa();
        NyxbornCourser card = new NyxbornCourser();
        card.setOwnerId(player2.getId());
        Permanent courser = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(courser.getId(), player2.getId());
        courser.tap();
        courser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToEndStep();
        harness.handlePermanentChosen(player1, courser.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Nyxborn Courser");
        assertThat(returned.getId()).isNotEqualTo(courser.getId());
        assertThat(returned.getCard().getOwnerId()).isEqualTo(player2.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getPlusOnePlusOneCounters()).isZero();
        assertThat(findPermanents(player2, "Nyxborn Courser")).isEmpty();
    }

    @Test
    @DisplayName("The flicker target must still be controlled by you when the trigger resolves")
    void doesNotFlickerCreatureThatChangesController() {
        addThassa();
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        advanceToEndStep();
        harness.handlePermanentChosen(player1, courser.getId());

        gd.playerBattlefields.get(player1.getId()).remove(courser);
        gd.playerBattlefields.get(player2.getId()).add(courser);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(courser);
        assertThat(findPermanents(player1, "Nyxborn Courser")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability can tap your own creature while Thassa is tapped")
    void tapsOwnCreatureWithoutTappingThassaAsCost() {
        Permanent thassa = addThassa();
        thassa.tap();
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, courser.getId());
        harness.passBothPriorities();

        assertThat(courser.isTapped()).isTrue();
        assertThat(thassa.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Thassa triggers with no target even when no other creature is available")
    void triggersWithNoEligibleCreature() {
        addThassa();

        advanceToEndStep();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The end-step ability cannot target Thassa or an opponent's creature")
    void endStepCannotTargetSelfOrOpponentCreature() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        Permanent opposingCourser = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());

        advanceToEndStep();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, thassa.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCourser.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(thassa);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCourser);
    }

    private Permanent addThassa() {
        return harness.addToBattlefieldAndReturn(player1, new ThassaDeepDwelling());
    }

    private void addBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new RiptideTurtle());
        }
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
