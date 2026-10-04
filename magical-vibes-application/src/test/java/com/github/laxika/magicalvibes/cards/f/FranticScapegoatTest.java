package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.d.Deduce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FranticScapegoat.class, FugitiveCodebreaker.class, CallTheCavalry.class, Deduce.class, Shock.class})
class FranticScapegoatTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldSuspected() {
        harness.castFromHand(player1, new FranticScapegoat(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Frantic Scapegoat").isSuspected()).isTrue();
    }

    @Test
    void maySuspectOneOtherCreatureAndClearItself() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        castCodebreaker();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent codebreaker = findPermanent(player1, "Fugitive Codebreaker");
        assertThat(codebreaker.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    @Test
    void onlyTheEnteringCreatureCanBeSuspected() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        Permanent existingCodebreaker = addCreatureReady(player1, new FugitiveCodebreaker());
        castCodebreaker();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent enteringCodebreaker = findPermanents(player1, "Fugitive Codebreaker").getLast();
        assertThat(existingCodebreaker.isSuspected()).isFalse();
        assertThat(enteringCodebreaker.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    @Test
    @CardUsed(CallTheCavalry.class)
    void tokenBatchCreatesOneMayTrigger() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.castFromHand(player1, new CallTheCavalry(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent chosenKnight = findPermanents(player1, "Knight").getFirst();
        harness.handlePermanentChosen(player1, chosenKnight.getId());

        assertThat(chosenKnight.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningLeavesBothCreaturesUnchanged() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        castCodebreaker();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(scapegoat.isSuspected()).isTrue();
        assertThat(findPermanent(player1, "Fugitive Codebreaker").isSuspected()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void unsuspectedScapegoatDoesNotTrigger() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        castCodebreaker();

        assertThat(scapegoat.isSuspected()).isFalse();
        assertThat(findPermanent(player1, "Fugitive Codebreaker").isSuspected()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void becomingUnsuspectedBeforeResolutionPreventsTransfer() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.castFromHand(player1, new FugitiveCodebreaker(), "{1}{R}");
        harness.passBothPriorities();
        scapegoat.setSuspected(false);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Codebreaker").isSuspected()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(Deduce.class)
    void noncreatureTokenDoesNotTrigger() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.castFromHand(player1, new Deduce(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clue")).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(scapegoat.isSuspected()).isTrue();
    }

    @Test
    @CardUsed(Shock.class)
    void transferStillResolvesAfterSuspectedScapegoatDies() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.castFromHand(player1, new FugitiveCodebreaker(), "{1}{R}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, scapegoat.getId());
        harness.assertInGraveyard(player1, "Frantic Scapegoat");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Fugitive Codebreaker").isSuspected()).isTrue();
    }

    @Test
    void alreadySuspectedEnteringCreatureStillClearsScapegoat() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        harness.castFromHand(player1, new FugitiveCodebreaker(), "{1}{R}");
        harness.passBothPriorities();
        Permanent codebreaker = findPermanent(player1, "Fugitive Codebreaker");
        codebreaker.setSuspected(true);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(codebreaker.isSuspected()).isTrue();
        assertThat(scapegoat.isSuspected()).isFalse();
    }

    @Test
    void opponentCreatureDoesNotTrigger() {
        Permanent scapegoat = addCreatureReady(player1, new FranticScapegoat());
        scapegoat.setSuspected(true);
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new FugitiveCodebreaker());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opposingCreature.isSuspected()).isFalse();
        assertThat(scapegoat.isSuspected()).isTrue();
    }

    private void castCodebreaker() {
        harness.castFromHand(player1, new FugitiveCodebreaker(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
