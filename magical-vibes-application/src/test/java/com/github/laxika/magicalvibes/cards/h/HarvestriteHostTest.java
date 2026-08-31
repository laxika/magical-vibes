package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZodiacRabbit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarvestriteHost.class, GrizzlyBears.class, ZodiacRabbit.class})
class HarvestriteHostTest extends BaseCardTest {

    @Test
    void ownEntryBoostsTargetWithoutDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        castHost();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    void secondRabbitEntryDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);

        castHost();
        resolveTrigger(target);
        castRabbit();
        resolveTrigger(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void fizzledTriggerDoesNotCountTowardSecondResolution() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);

        castHost();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        gd.playerBattlefields.get(player1.getId()).remove(firstTarget);
        harness.passBothPriorities();

        castRabbit();
        resolveTrigger(secondTarget);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        castRabbit();
        resolveTrigger(secondTarget);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void castHost() {
        harness.setHand(player1, List.of(new HarvestriteHost()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castRabbit() {
        harness.setHand(player1, List.of(new ZodiacRabbit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void resolveTrigger(Permanent target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
