package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PleaForPower;
import com.github.laxika.magicalvibes.cards.v.Vault11VotersDilemma;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ModelOfUnity.class, PleaForPower.class, Forest.class, Vault11VotersDilemma.class})
class ModelOfUnityTest extends BaseCardTest {

    @Test
    void addsOneManaOfChosenColorAndTaps() {
        Permanent model = harness.addToBattlefieldAndReturn(player1, new ModelOfUnity());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(model.isTapped()).isTrue();
    }

    @Test
    void controllerAndMatchingVoterMayScryTwo() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void nonMatchingVoterIsNotOfferedScry() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void matchingOpponentCanScryWhenControllerDeclines() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.TIME);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void opponentControllerMayScryEvenWhenTheirChoiceDoesNotWin() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Forest remaining = new Forest();
        harness.setLibrary(player2, List.of(remaining));
        harness.enterBattlefieldAndReturn(player2, new ModelOfUnity());
        castPleaForPower();

        harness.handleListChoice(player1, ChoiceContext.PleaForPowerChoice.KNOWLEDGE);
        harness.handleListChoice(player2, ChoiceContext.PleaForPowerChoice.TIME);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(remaining);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void triggersAfterSecretCreatureVotingFinishes() {
        harness.enterBattlefieldAndReturn(player1, new ModelOfUnity());
        Permanent saga = harness.enterBattlefieldAndReturn(player1, new Vault11VotersDilemma());
        resolveAllTriggers();
        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(soldier.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(soldier.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castPleaForPower() {
        harness.castFromHand(player1, new PleaForPower(), "{3}{U}");
        harness.passBothPriorities();
    }
}
