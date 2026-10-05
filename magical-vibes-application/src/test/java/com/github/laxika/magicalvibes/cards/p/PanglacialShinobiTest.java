package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.Entomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PanglacialShinobi.class, Entomb.class, Forest.class, GrizzlyBears.class})
class PanglacialShinobiTest extends BaseCardTest {

    @Test
    void libraryNinjutsuShufflesAttackerAndEntersTappedAndAttacking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        PanglacialShinobi shinobi = new PanglacialShinobi();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new Entomb()));
        harness.setLibrary(player1, List.of(shinobi, forest));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), shinobi));
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, attacker.getId());
        assertThat(gameData.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(attacker.getCard().getId()));
        assertThat(gameData.stack).anyMatch(entry -> entry.getCard().getId().equals(shinobi.getId()));

        search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), forest));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);

        Permanent entered = findPermanent(player1, "Panglacial Shinobi");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(entered.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    void canFindShinobiWithoutActivatingLibraryNinjutsu() {
        PanglacialShinobi shinobi = new PanglacialShinobi();
        harness.setHand(player1, List.of(new Entomb()));
        harness.setLibrary(player1, List.of(shinobi));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), shinobi));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shinobi);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFindAttackerShuffledIntoLibraryWhileActivatingLibraryNinjutsu() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        PanglacialShinobi shinobi = new PanglacialShinobi();
        harness.setHand(player1, List.of(new Entomb()));
        harness.setLibrary(player1, List.of(shinobi, new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), shinobi));
        harness.handlePermanentChosen(player1, attacker.getId());

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, indexOfCard(search.params().cards(), attacker.getCard()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, harness::passBothPriorities);
        assertThat(findPermanent(player1, "Panglacial Shinobi").isAttacking()).isTrue();
    }

    @Test
    void drawsWhenItDealsCombatDamageToAPlayer() {
        Permanent shinobi = addCreatureReady(player1, new PanglacialShinobi());
        shinobi.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private int indexOfCard(List<Card> cards, Card wanted) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getId().equals(wanted.getId())) {
                return i;
            }
        }
        throw new AssertionError("Card was not offered in the library search");
    }
}
