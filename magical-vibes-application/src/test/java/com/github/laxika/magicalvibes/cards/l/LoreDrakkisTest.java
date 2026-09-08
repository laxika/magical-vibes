package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoreDrakkis.class, Opt.class, Divination.class, GrizzlyBears.class})
class LoreDrakkisTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns a chosen instant or sorcery from your graveyard to your hand")
    void mutatingReturnsChosenSpellToHand() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card opt = new Opt();
        Card divination = new Divination();
        harness.setGraveyard(player1, List.of(opt, divination));

        triggerMutation(drakkis);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opt.getId(), divination.getId());

        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("Mutating cannot target a non-instant or non-sorcery card")
    void mutatingCannotTargetPermanentCard() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        triggerMutation(drakkis);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Mutating cannot target an instant or sorcery in an opponent's graveyard")
    void mutatingCannotTargetOpponentGraveyard() {
        Permanent drakkis = addCreatureReady(player1, new LoreDrakkis());
        Card opt = new Opt();
        harness.setGraveyard(player2, List.of(opt));

        triggerMutation(drakkis);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Opt");
    }

    private void triggerMutation(Permanent drakkis) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, drakkis, List.of(drakkis.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
