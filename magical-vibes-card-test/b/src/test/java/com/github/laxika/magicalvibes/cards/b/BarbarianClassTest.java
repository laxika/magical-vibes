package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianClass.class, ContactOtherPlane.class, HillGiantHerdgorger.class, ContentiousPlan.class})
class BarbarianClassTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(9));
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void levelTwoAbilityDoesNotTriggerBeforeTheClassReachesLevelTwo() {
        harness.addToBattlefield(player1, new BarbarianClass());
        harness.addToBattlefield(player1, new HillGiantHerdgorger());
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelTwoAbilityBoostsTargetCreatureAndGrantsMenaceUntilEndOfTurn() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        levelUpToTwo(barbarianClass);
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(creature.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void levelThreeGivesHasteToCreaturesYouControl() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        levelUpToThree(barbarianClass);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @CardUsed({BarbarianClass.class, ContentiousPlan.class, HillGiantHerdgorger.class})
    void classLevelDoesNotMakeTheClassEligibleForProliferate() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        levelUpToTwo(barbarianClass);
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void advantageKeepsTheHigherRollAndStacksAcrossClasses() {
        harness.addToBattlefield(player1, new BarbarianClass());
        harness.addToBattlefield(player1, new BarbarianClass());
        SequenceD20RollService rolls = new SequenceD20RollService(2, 9, 5);
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", rolls);
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        harness.castAndResolveInstant(player1, 0);

        assertThat(rolls.calls).isEqualTo(3);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("Contact Other Plane: 9."));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentsClassDoesNotAddDiceToYourRoll() {
        harness.addToBattlefield(player2, new BarbarianClass());
        SequenceD20RollService rolls = new SequenceD20RollService(2);
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", rolls);
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        harness.castAndResolveInstant(player1, 0);

        assertThat(rolls.calls).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void levelThreeRetainsTheLevelTwoTriggerAndIgnoredDiceDoNotCreateExtraTriggers() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        levelUpToThree(barbarianClass);
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSkipLevelTwoOrLevelUpOutsideYourMainPhase() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(barbarianClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(barbarianClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pendingLevelTwoTriggerSurvivesClassRemovalAndExpiresAtEndOfTurn() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        levelUpToTwo(barbarianClass);
        prepareRoll(List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger()));
        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, barbarianClass));

        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    private void prepareRoll(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ContactOtherPlane()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void levelUpToTwo(Permanent barbarianClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, battlefieldIndex(barbarianClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent barbarianClass) {
        levelUpToTwo(barbarianClass);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, battlefieldIndex(barbarianClass), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private static final class SequenceD20RollService extends D20RollService {
        private final int[] results;
        private int calls;

        private SequenceD20RollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll() {
            return results[calls++];
        }
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
