package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Cremate;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.cards.k.KavuRunner;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootingKavu.class, KavuAggressor.class, KavuRunner.class, Opt.class, Rout.class, Cremate.class})
class RootingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger exiles Rooting Kavu and shuffles creature cards into the library")
    void acceptingDeathTriggerExilesAndShufflesCreatureCards() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card rootingKavu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new KavuAggressor();
        Card hillGiant = new KavuRunner();
        Card shock = new Opt();
        harness.setGraveyard(player1, List.of(bears, shock, hillGiant));

        destroyRootingKavu();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(rootingKavu.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId(), hillGiant.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(shock.getId())
                .doesNotContain(bears.getId(), hillGiant.getId(), rootingKavu.getId());
    }

    @Test
    @DisplayName("Declining the death trigger leaves Rooting Kavu and the graveyard cards in the graveyard")
    void decliningDeathTriggerDoesNothing() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card rootingKavu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card bears = new KavuAggressor();
        Card shock = new Opt();
        harness.setGraveyard(player1, List.of(bears, shock));

        destroyRootingKavu();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(rootingKavu.getId(), bears.getId(), shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(bears.getId(), rootingKavu.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(rootingKavu.getId());
    }

    @Test
    @DisplayName("Accepting the death trigger only shuffles creature cards from the controller's graveyard")
    void acceptingDeathTriggerDoesNotShuffleOpponentsGraveyard() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card ownCreature = new KavuAggressor();
        Card opponentCreature = new KavuRunner();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        destroyRootingKavu();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(opponentCreature.getId());
    }

    @Test
    @DisplayName("Accepting after Rooting Kavu left the graveyard does not shuffle creature cards")
    void acceptingAfterSourceLeftGraveyardDoesNotShuffle() {
        harness.addToBattlefield(player1, new RootingKavu());
        Card rootingKavu = gd.playerBattlefields.get(player1.getId()).getFirst().getCard();
        Card creature = new KavuAggressor();
        Card nonCreature = new Opt();
        harness.setGraveyard(player1, List.of(creature, nonCreature));

        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Cremate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, rootingKavu.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId(), nonCreature.getId());
    }

    private void destroyRootingKavu() {
        harness.setHand(player1, List.of(new Rout()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
