package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GamekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger exiles Gamekeeper and finds the first creature")
    void acceptingDeathTriggerFindsCreature() {
        Card gamekeeperCard = putGamekeeperOnBattlefield();
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card island = new Island();
        setLibrary(forest, creature, island);

        destroyGamekeeper();
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gamekeeperCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gamekeeperCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .contains(creature);
    }

    @Test
    @DisplayName("Declining the death trigger leaves Gamekeeper in the graveyard and does not reveal")
    void decliningDeathTriggerDoesNothing() {
        Card gamekeeperCard = putGamekeeperOnBattlefield();
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        setLibrary(forest, creature);

        destroyGamekeeper();
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gamekeeperCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(gamekeeperCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard())
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Accepting with no creature puts the entire library into the graveyard")
    void acceptingWithNoCreatureMillsEntireLibrary() {
        Card gamekeeperCard = putGamekeeperOnBattlefield();
        Card forest = new Forest();
        Card island = new Island();
        setLibrary(forest, island);

        destroyGamekeeper();
        resolveDeathTriggerToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gamekeeperCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Card putGamekeeperOnBattlefield() {
        harness.addToBattlefield(player1, new Gamekeeper());
        Permanent permanent = gd.playerBattlefields.get(player1.getId()).getFirst();
        return permanent.getCard();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }

    private void destroyGamekeeper() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void resolveDeathTriggerToMayChoice() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
