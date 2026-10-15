package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CurseOfClingingWebs;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedReturnCurseAttachedToPlayer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LyndeCheerfulTormentor.class, CurseOfClingingWebs.class})
class LyndeCheerfulTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Curse from its owner's graveyard attached to Lynde's controller")
    void returnsCurseAtNextEndStep() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        curse.setAttachedTo(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Lynde, Cheerful Tormentor"));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedReturnCurseAttachedToPlayer.class)).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Curse of Clinging Webs");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Curse of Clinging Webs");
        assertThat(returned.getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    void triggersForOwnedCurseControlledByOpponent() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        CurseOfClingingWebs card = new CurseOfClingingWebs();
        card.setOwnerId(player1.getId());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, card);
        curse.setAttachedTo(player1.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse));

        harness.assertInGraveyard(player1, "Curse of Clinging Webs");
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Lynde, Cheerful Tormentor"));
    }

    @Test
    void doesNotTriggerForOpponentOwnedCurseUnderYourControl() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        CurseOfClingingWebs card = new CurseOfClingingWebs();
        card.setOwnerId(player2.getId());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, card);
        curse.setAttachedTo(player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse));

        harness.assertInGraveyard(player2, "Curse of Clinging Webs");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void delayedReturnDoesNotFollowCurseThatLeftAndReenteredGraveyard() {
        Permanent lynde = harness.addToBattlefieldAndReturn(player1, new LyndeCheerfulTormentor());
        CurseOfClingingWebs card = new CurseOfClingingWebs();
        Permanent curse = harness.addToBattlefieldAndReturn(player1, card);
        curse.setAttachedTo(player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse));
        harness.passBothPriorities();

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lynde);
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, card.getId());
        });
        Permanent reentered = harness.addToBattlefieldAndReturn(player1, card);
        reentered.setAttachedTo(player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, reentered));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Curse of Clinging Webs");
        harness.assertInGraveyard(player1, "Curse of Clinging Webs");
    }

    @Test
    void decliningUpkeepLeavesCurseAttachedAndDoesNotDraw() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        curse.setAttachedTo(player1.getId());
        harness.setLibrary(player1, List.of(new CurseOfClingingWebs(), new CurseOfClingingWebs()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(curse.getAttachedTo()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void movesOnlyChosenCurseIncludingOneControlledByOpponent() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CurseOfClingingWebs());
        first.setAttachedTo(player1.getId());
        second.setAttachedTo(player1.getId());
        harness.setLibrary(player1, List.of(new CurseOfClingingWebs(), new CurseOfClingingWebs()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getAttachedTo()).isEqualTo(player1.getId());
        assertThat(second.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void doesNotDrawWhenNoCurseIsAttachedToYou() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        curse.setAttachedTo(player2.getId());
        harness.setLibrary(player1, List.of(new CurseOfClingingWebs(), new CurseOfClingingWebs()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Moves an attached Curse to an opponent and draws two cards")
    void attachesCurseToOpponentAndDraws() {
        harness.addToBattlefield(player1, new LyndeCheerfulTormentor());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfClingingWebs());
        curse.setAttachedTo(player1.getId());
        harness.setLibrary(player1, List.of(new CurseOfClingingWebs(), new CurseOfClingingWebs()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(curse.getAttachedTo()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }
}
