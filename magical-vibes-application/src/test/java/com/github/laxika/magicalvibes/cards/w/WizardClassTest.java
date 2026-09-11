package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WizardClass.class, Forest.class, GrizzlyBears.class})
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

        assertThat(wizardClass.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void levelThreePutsCounterOnTargetCreatureWhenYouDraw() {
        Permanent wizardClass = harness.addToBattlefieldAndReturn(player1, new WizardClass());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        levelUpToThree(wizardClass);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
