package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisfortuneTeller.class, Forest.class, GrizzlyBears.class, Shock.class, DryadArbor.class})
class MisfortuneTellerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiling a creature card creates a 2/2 Rogue token")
    void etbExilingCreatureCreatesRogue() {
        Card creature = new GrizzlyBears();

        castAndResolveEnterTrigger(creature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB exiling a land card creates a Treasure token")
    void etbExilingLandCreatesTreasure() {
        Card land = new Forest();

        castAndResolveEnterTrigger(land);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB exiling another card gains 3 life")
    void etbExilingOtherCardGainsLife() {
        Card other = new Shock();

        castAndResolveEnterTrigger(other);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(other);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Combat damage to a player also exiles a graveyard card and resolves its branch")
    void combatDamageTriggerCreatesTreasureForLand() {
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        addCreatureReady(player1, new MisfortuneTeller());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        chooseGraveyardCard(land);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Exiling a land creature creates both a Rogue and a Treasure")
    void etbExilingLandCreatureCreatesBothTokens() {
        Card landCreature = new DryadArbor();

        castAndResolveEnterTrigger(landCreature);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(landCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ETB trigger can target the controller's own graveyard")
    void etbCanExileFromOwnGraveyard() {
        Card creature = new MisfortuneTeller();
        harness.setGraveyard(player1, List.of(creature));

        castAndResolveEnterTriggerWithoutSettingGraveyard(creature);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player2, "Rogue")).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard gives no life or tokens")
    void targetLeavingGraveyardDoesNotGiveBonus() {
        Card other = new Shock();
        harness.setGraveyard(player2, List.of(other));
        castTellerAndResolveSpell();
        chooseGraveyardCard(other);
        harness.setGraveyard(player2, List.of());

        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering with empty graveyards gives no life or tokens")
    void emptyGraveyardsDoNotGiveBonus() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castTellerAndResolveSpell();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage exiling a creature creates a Rogue")
    void combatDamageExilingCreatureCreatesRogue() {
        Card creature = new MisfortuneTeller();
        harness.setGraveyard(player2, List.of(creature));
        addCreatureReady(player1, new MisfortuneTeller());

        declareAttackers(List.of(0));
        resolveCombat();
        chooseGraveyardCard(creature);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Combat damage exiling a noncreature nonland gains life")
    void combatDamageExilingOtherCardGainsLife() {
        Card other = new Shock();
        harness.setGraveyard(player2, List.of(other));
        addCreatureReady(player1, new MisfortuneTeller());

        declareAttackers(List.of(0));
        resolveCombat();
        chooseGraveyardCard(other);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(other);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Combat damage exiling a land creature creates both tokens")
    void combatDamageExilingLandCreatureCreatesBothTokens() {
        Card landCreature = new DryadArbor();
        harness.setGraveyard(player2, List.of(landCreature));
        addCreatureReady(player1, new MisfortuneTeller());

        declareAttackers(List.of(0));
        resolveCombat();
        chooseGraveyardCard(landCreature);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(landCreature);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The targeted ETB ability resolves after its source dies")
    void etbTriggerResolvesAfterSourceDies() {
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        castTellerAndResolveSpell();
        chooseGraveyardCard(land);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Misfortune Teller").getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Misfortune Teller")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocker does not trigger graveyard exile")
    void combatDamageToCreatureDoesNotTrigger() {
        Card other = new Shock();
        harness.setGraveyard(player2, List.of(other));
        addCreatureReady(player1, new MisfortuneTeller());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castAndResolveEnterTriggerWithoutSettingGraveyard(Card graveyardCard) {
        castTellerAndResolveSpell();
        chooseGraveyardCard(graveyardCard);
        resolveAllTriggers();
    }

    private void castTellerAndResolveSpell() {
        harness.setHand(player1, List.of(new MisfortuneTeller()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castAndResolveEnterTrigger(Card graveyardCard) {
        harness.setGraveyard(player2, List.of(graveyardCard));
        castAndResolveEnterTriggerWithoutSettingGraveyard(graveyardCard);
    }

    private void chooseGraveyardCard(Card card) {
        if (gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) != null) {
            harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        } else {
            harness.handleGraveyardCardChosen(player1, 0);
        }
    }
}
