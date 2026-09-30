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
        harness.clearPriorityPassed();
        gs.advanceStep(gd);

        Permanent returned = findPermanent(player1, "Curse of Clinging Webs");
        assertThat(returned.getAttachedTo()).isEqualTo(player1.getId());
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
