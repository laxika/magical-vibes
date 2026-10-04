package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TendThePests;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EfreetFlamepainter.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        Shock.class, Naturalize.class, TendThePests.class})
class EfreetFlamepainterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets only instant and sorcery cards from your graveyard")
    void combatDamageTargetsOnlyOwnInstantsAndSorceries() {
        Card ownInstant = new Shock();
        Card ownSorcery = new CounselOfTheSoratami();
        Card ownCreature = new GrizzlyBears();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownInstant, ownSorcery, ownCreature)));
        harness.setGraveyard(player2, List.of(opponentInstant));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownInstant.getId(), ownSorcery.getId());
    }

    @Test
    @DisplayName("Combat damage lets you cast the chosen sorcery for free and exiles it")
    void castsChosenSorceryForFreeAndExilesIt() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(counsel)));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Combat damage with no legal card does not prompt")
    void noLegalCardDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        attackDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining the cast leaves the targeted card in the graveyard")
    void decliningCastLeavesCardInGraveyard() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(counsel)));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(counsel);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("A graveyard target removed before resolution cannot be cast")
    void removedGraveyardTargetCannotBeCast() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(counsel)));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(counsel);
        gd.addToExile(player1.getId(), counsel);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(counsel);
    }

    @Test
    @DisplayName("A spell with no legal targets remains in the graveyard")
    void uncastableSpellIsNotExiled() {
        Card naturalize = new Naturalize();
        harness.setGraveyard(player1, new ArrayList<>(List.of(naturalize)));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(naturalize.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(naturalize);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(naturalize);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(naturalize.getId()));
    }

    @Test
    @DisplayName("Casting for free still requires sacrificing a creature for Tend the Pests")
    void freeCastPaysMandatorySacrificeCost() {
        Card tend = new TendThePests();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(tend)));

        attackDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(tend.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Pest"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(tend);
    }

    private void attackDealingDamage() {
        Permanent efreet = addCreatureReady(player1, new EfreetFlamepainter());
        efreet.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
