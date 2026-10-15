package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExhumerThrull.class, DaggerclawImp.class, DouseInGloom.class})
class ExhumerThrullTest extends BaseCardTest {

    @Test
    void enteringReturnsTargetCreatureCardFromGraveyardToHand() {
        Card creature = new DaggerclawImp();
        Card noncreature = new DouseInGloom();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setHand(player1, List.of(new ExhumerThrull()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Daggerclaw Imp");
        harness.assertInGraveyard(player1, "Douse in Gloom");
    }

    @Test
    void hauntedCreatureDeathReturnsTargetCreatureCardFromGraveyardToHand() {
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        Card creature = new DaggerclawImp();
        harness.setGraveyard(player1, List.of(creature));
        Permanent exhumer = harness.addToBattlefieldAndReturn(player1, new ExhumerThrull());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, exhumer));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice hauntChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(hauntChoice).isNotNull();
        assertThat(hauntChoice.validIds()).containsExactly(hauntedCreature.getId());
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Exhumer Thrull");

        destroyWithDouseInGloom(hauntedCreature.getId());

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice).isNotNull();
        assertThat(returnChoice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Daggerclaw Imp");
    }

    private void destroyWithDouseInGloom(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    @Test
    void enteringWithoutOwnCreatureCardsDoesNotReturnAnOpponentsCard() {
        harness.setGraveyard(player1, List.of(new DouseInGloom()));
        harness.setGraveyard(player2, List.of(new DaggerclawImp()));
        harness.setHand(player1, List.of(new ExhumerThrull()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Exhumer Thrull");
        harness.assertInGraveyard(player1, "Douse in Gloom");
        harness.assertInGraveyard(player2, "Daggerclaw Imp");
        harness.assertNotInHand(player1, "Daggerclaw Imp");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hauntDoesNotExileThrullWhenItsTargetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        Permanent exhumer = harness.addToBattlefieldAndReturn(player1, new ExhumerThrull());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, exhumer));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        destroyWithDouseInGloom(target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Exhumer Thrull");
        harness.assertInGraveyard(player2, "Daggerclaw Imp");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
