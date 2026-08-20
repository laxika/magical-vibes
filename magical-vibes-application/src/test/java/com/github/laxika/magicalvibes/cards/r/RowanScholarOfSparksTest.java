package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RowanScholarOfSparksTest extends BaseCardTest {

    @Test
    void rowanDealsOneDamageWithoutThreeDraws() {
        addReadyRowan(5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void rowanDealsThreeDamageAfterThreeDraws() {
        addReadyRowan(5);
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    void rowanEmblemMayPayToCopyInstant() {
        addReadyRowan(5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Terminate"));
    }

    @Test
    void willDrawsTwoCardsAndExilesPermanentsForElementalTokens() {
        RowanScholarOfSparks card = new RowanScholarOfSparks();
        Permanent will = harness.addToBattlefieldAndReturn(player1, card);
        will.setCard(card.getBackFaceCard());
        will.setTransformed(true);
        will.setCounterCount(CounterType.LOYALTY, 7);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        will.setCounterCount(CounterType.LOYALTY, 7);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Elemental")
                        && permanent.getEffectivePower() == 4
                        && permanent.getEffectiveToughness() == 4);
    }

    private Permanent addReadyRowan(int loyalty) {
        Permanent rowan = harness.addToBattlefieldAndReturn(player1, new RowanScholarOfSparks());
        rowan.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return rowan;
    }
}
