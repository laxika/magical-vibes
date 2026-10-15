package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinscBelovedRanger.class, GrizzlyBears.class})
class MinscBelovedRangerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates legendary Boo")
    void enteringCreatesBoo() {
        harness.castFromHand(player1, new MinscBelovedRanger(), "{R}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent boo = findPermanents(player1, "Boo").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(boo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(boo.getEffectivePower()).isEqualTo(1);
        assertThat(boo.getEffectiveToughness()).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(boo, CardSubtype.HAMSTER)).isTrue();
        assertThat(gqs.hasKeyword(gd, boo, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, boo, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability sets base P/T and adds Giant until end of turn")
    void activatedAbilityChangesTargetUntilEndOfTurn() {
        addCreatureReady(player1, new MinscBelovedRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears).contains(CardSubtype.GIANT)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears).contains(CardSubtype.GIANT)).isFalse();
    }

    @Test
    void canTargetItselfWhileSummoningSick() {
        Permanent minsc = harness.addToBattlefieldAndReturn(player1, new MinscBelovedRanger());
        minsc.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 5, minsc.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minsc)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minsc)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, minsc))
                .contains(CardSubtype.HUMAN, CardSubtype.RANGER, CardSubtype.GIANT);
    }

    @Test
    void zeroCanPutMinscIntoGraveyardWithoutMana() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());

        harness.activateAbility(player1, 0, 0, minsc.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Minsc, Beloved Ranger");
        harness.assertInGraveyard(player1, "Minsc, Beloved Ranger");
    }

    @Test
    void countersStillApplyWhenBasePowerAndToughnessBecomeZero() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        minsc.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, minsc.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Minsc, Beloved Ranger");
        assertThat(gqs.getEffectivePower(gd, minsc)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, minsc)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, minsc)).contains(CardSubtype.GIANT);
    }

    @Test
    void laterActivationOverwritesEarlierBasePowerAndToughness() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 5, minsc.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, minsc.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minsc)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, minsc)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, minsc))
                .contains(CardSubtype.HUMAN, CardSubtype.RANGER, CardSubtype.GIANT);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new MinscBelovedRanger());
        Permanent opponent = addCreatureReady(player2, new MinscBelovedRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, minsc.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateOnOpponentsTurn() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, minsc.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherAbilityIsOnStack() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, minsc.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, minsc.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void abilityStillResolvesAfterMinscLeavesBattlefield() {
        Permanent minsc = addCreatureReady(player1, new MinscBelovedRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 4, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(minsc);
        gd.playerGraveyards.get(player1.getId()).add(minsc.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears))
                .contains(CardSubtype.BEAR, CardSubtype.GIANT);
    }

    @Test
    void abilityDoesNothingIfTargetChangesControllerBeforeResolution() {
        addCreatureReady(player1, new MinscBelovedRanger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 4, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.BEAR)
                .doesNotContain(CardSubtype.GIANT);
    }
}
