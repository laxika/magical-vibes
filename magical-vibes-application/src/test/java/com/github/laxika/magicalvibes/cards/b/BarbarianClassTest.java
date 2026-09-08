package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContactOtherPlane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({BarbarianClass.class, ContactOtherPlane.class, GrizzlyBears.class})
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareRoll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelTwoAbilityBoostsTargetCreatureAndGrantsMenaceUntilEndOfTurn() {
        Permanent barbarianClass = harness.addToBattlefieldAndReturn(player1, new BarbarianClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        levelUpToTwo(barbarianClass);
        prepareRoll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        levelUpToThree(barbarianClass);

        assertThat(barbarianClass.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
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
