package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlderRavengardMarshal.class, GrizzlyBears.class, ThayanEvokers.class})
class UlderRavengardMarshalTest extends BaseCardTest {

    @Test
    void entersAndGivesAnotherNontokenCreatureDoubleTeamUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ulder = harness.enterBattlefieldAndReturn(player1, new UlderRavengardMarshal());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, ulder, Keyword.DOUBLE_TEAM)).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void attackTriggerConjuresDuplicateOfAnotherNontokenAttackingCreature() {
        addReadyCreature(new UlderRavengardMarshal());
        Permanent bears = addReadyCreature(new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void enterTriggerExcludesOpponentsCreaturesAndTokens() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        harness.enterBattlefieldAndReturn(player1, new UlderRavengardMarshal());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isTrue();
    }

    @Test
    void attackTriggerExcludesTokensAndNonattackingCreatures() {
        addReadyCreature(new UlderRavengardMarshal());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Card token = new GrizzlyBears();
        token.setToken(true);
        addReadyCreature(token);
        addReadyCreature(new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void attackTriggerDoesNotConjureWhenTargetLeavesBattlefield() {
        addReadyCreature(new UlderRavengardMarshal());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void grantedDoubleTeamConjuresWhenRecipientAttacksWithoutUlder() {
        Permanent bears = addReadyCreature(new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new UlderRavengardMarshal());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void attackConjureTriggersThayanEvokers() {
        addReadyCreature(new UlderRavengardMarshal());
        Permanent bears = addReadyCreature(new GrizzlyBears());
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
