package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RemoveSoul;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmetSelchOfTheThirdSeat.class, CounselOfTheSoratami.class, GrizzlyBears.class, Shock.class, RemoveSoul.class})
class EmetSelchOfTheThirdSeatTest extends BaseCardTest {

    @Test
    @DisplayName("Costs two less when casting a spell from your graveyard")
    void reducesGraveyardSpellCost() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(counsel.getId());

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
    }

    @Test
    @DisplayName("Declining the cast allows another trigger in the same turn")
    void decliningCastAllowsAnotherTrigger() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card firstCounsel = new CounselOfTheSoratami();
        Card secondCounsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(firstCounsel, secondCounsel));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstCounsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Only your instant and sorcery cards are legal targets")
    void onlyOwnInstantAndSorceryCardsAreLegalTargets() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card ownShock = new Shock();
        Card ownCreature = new GrizzlyBears();
        Card opponentShock = new Shock();
        Card triggeringShock = new Shock();
        harness.setGraveyard(player1, List.of(ownShock, ownCreature));
        harness.setGraveyard(player2, List.of(opponentShock));
        harness.setHand(player1, List.of(triggeringShock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownShock.getId(), triggeringShock.getId());
    }

    @Test
    @DisplayName("Successfully casting a card prevents another trigger that turn")
    void successfulCastUsesOncePerTurnOpportunity() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("The graveyard cost reduction does not remove colored mana requirements")
    void cannotCastWithoutRequiredColoredMana() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(counsel);
    }

    @Test
    @DisplayName("A card that cannot be cast for lack of legal targets stays in the graveyard")
    void noLegalSpellTargetsDoesNotExileCard() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card removeSoul = new RemoveSoul();
        harness.setGraveyard(player1, List.of(removeSoul));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(removeSoul.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(removeSoul);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(removeSoul);
    }

    @Test
    @DisplayName("Can cast a counterspell from the graveyard targeting a creature spell")
    void canCastCounterspellWithLegalStackTarget() {
        harness.addToBattlefield(player1, new EmetSelchOfTheThirdSeat());
        Card removeSoul = new RemoveSoul();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removeSoul));
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(removeSoul.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(removeSoul);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
