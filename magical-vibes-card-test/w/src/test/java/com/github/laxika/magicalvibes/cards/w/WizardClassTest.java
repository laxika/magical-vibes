package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WizardClass.class, Forest.class, HillGiantHerdgorger.class, IntoTheRoil.class})
class WizardClassTest extends BaseCardTest {

    @Test
    void levelTwoDrawsTwoCards() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(wizardClass), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(wizardClass.getClassLevel()).isEqualTo(2);
        assertThat(wizardClass.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelThreePutsCounterOnTargetCreatureWhenYouDraw() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        levelUpToThree(wizardClass);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void levelThreeDoesNotTargetAnOpponentsCreature() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        levelUpToThree(wizardClass);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void levelOnePreventsDiscardDuringCleanup() {
        harness.addToBattlefield(player1, new WizardClass());
        harness.setHand(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }

    @Test
    void opponentStillDiscardsDuringCleanup() {
        harness.addToBattlefield(player1, new WizardClass());
        harness.setHand(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.getGameService().advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    @Test
    void levelTwoDrawDoesNotGiveCreatureCounters() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, battlefieldIndex(wizardClass), 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelThreeRequiresLevelTwoAndCannotBeRepeated() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 20);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(wizardClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        levelUpToThree(wizardClass);

        assertThat(wizardClass.getClassLevel()).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(wizardClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(wizardClass), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainingLevelsRequiresSorceryTiming() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(wizardClass), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wizardClass.getClassLevel()).isEqualTo(1);
    }

    @Test
    void opponentDrawDoesNotTriggerLevelThree() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        levelUpToThree(wizardClass);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachDrawTriggersSeparatelyAtLevelThree() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest()));
        levelUpToThree(wizardClass);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({WizardClass.class, Forest.class, HillGiantHerdgorger.class, IntoTheRoil.class})
    void drawTriggerResolvesAfterClassReturnsToHand() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        levelUpToThree(wizardClass);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, wizardClass.getId());
        harness.assertNotOnBattlefield(player1, "Wizard Class");
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    @Test
    void levelOneDrawDoesNotTriggerCreatureCounters() {
        harness.addToBattlefield(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private void levelUpToThree(Permanent wizardClass) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.activateAbility(player1, battlefieldIndex(wizardClass), 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        prepareForSorcery();
        harness.activateAbility(player1, battlefieldIndex(wizardClass), 1, null, null);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
