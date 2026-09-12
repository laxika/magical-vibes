package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.r.RapidDecay;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.cards.s.ScentOfJasmine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gamekeeper.class, AncientSilverback.class, ScentOfJasmine.class,
        RecklessAbandon.class, RapidDecay.class})
class GamekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger exiles Gamekeeper and finds the first creature")
    void acceptingDeathTriggerFindsCreature() {
        Permanent gamekeeper = putGamekeeperOnBattlefield();
        Card nonCreatureBefore = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        Card nonCreatureAfter = new ScentOfJasmine();
        setLibrary(nonCreatureBefore, creature, nonCreatureAfter);

        destroyGamekeeper(gamekeeper);
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gamekeeper.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonCreatureBefore)
                .doesNotContain(gamekeeper.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreatureAfter);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .contains(creature);
    }

    @Test
    @DisplayName("Declining the death trigger leaves Gamekeeper in the graveyard and does not reveal")
    void decliningDeathTriggerDoesNothing() {
        Permanent gamekeeper = putGamekeeperOnBattlefield();
        Card nonCreature = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        setLibrary(nonCreature, creature);

        destroyGamekeeper(gamekeeper);
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gamekeeper.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(gamekeeper.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreature, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Accepting with no creature puts the entire library into the graveyard")
    void acceptingWithNoCreatureMillsEntireLibrary() {
        Permanent gamekeeper = putGamekeeperOnBattlefield();
        Card firstNonCreature = new ScentOfJasmine();
        Card secondNonCreature = new ScentOfJasmine();
        setLibrary(firstNonCreature, secondNonCreature);

        destroyGamekeeper(gamekeeper);
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gamekeeper.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstNonCreature, secondNonCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if Gamekeeper leaves the graveyard before its trigger resolves")
    void doesNothingIfGamekeeperLeavesGraveyardBeforeTriggerResolves() {
        Permanent gamekeeper = putGamekeeperOnBattlefield();
        Card nonCreature = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        setLibrary(nonCreature, creature);

        destroyGamekeeper(gamekeeper);

        harness.setHand(player1, List.of(new RapidDecay()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(gamekeeper.getCard().getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gamekeeper.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gamekeeper.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreature, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .doesNotContain(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A stolen Gamekeeper uses its owner's graveyard and its controller's library")
    void stolenGamekeeperUsesOwnerGraveyardAndControllerLibrary() {
        Gamekeeper card = new Gamekeeper();
        card.setOwnerId(player1.getId());
        Permanent gamekeeper = harness.addToBattlefieldAndReturn(player1, card);
        gd.playerBattlefields.get(player1.getId()).remove(gamekeeper);
        gd.playerBattlefields.get(player2.getId()).add(gamekeeper);
        gd.stolenCreatures.put(gamekeeper.getId(), player1.getId());

        Card nonCreature = new ScentOfJasmine();
        Card creature = new AncientSilverback();
        harness.setLibrary(player2, List.of(nonCreature, creature));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .sacrificePermanentToGraveyard(gd, gamekeeper));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonCreature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard())
                .contains(creature);
    }

    private Permanent putGamekeeperOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new Gamekeeper());
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void destroyGamekeeper(Permanent gamekeeper) {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AncientSilverback());
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, gamekeeper.getId(), sacrifice.getId());
        harness.passBothPriorities();
    }

    private void resolveDeathTriggerToMayChoice() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
