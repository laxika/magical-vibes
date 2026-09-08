package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortalToPhyrexiaTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent chooses three creatures to sacrifice when it enters")
    void eachOpponentSacrificesThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spared = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new PortalToPhyrexia()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(spared.getId());
    }

    @Test
    @DisplayName("Upkeep returns a target creature from any graveyard as a Phyrexian")
    void upkeepReturnsCreatureFromAnyGraveyardAsPhyrexian() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(returned.getCard().getSubtypes()).contains(CardSubtype.BEAR);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Upkeep does not trigger without a creature card in any graveyard")
    void upkeepDoesNotTriggerWithoutCreatureCard() {
        harness.addToBattlefield(player1, new PortalToPhyrexia());
        harness.setGraveyard(player2, List.of(new PortalToPhyrexia()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
