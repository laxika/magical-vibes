package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EmptyTheWarrens;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterOrdeal.class, EmptyTheWarrens.class, GrizzlyBears.class, Shock.class})
class BitterOrdealTest extends BaseCardTest {

    @Test
    void exilesCardFromTargetPlayersLibrary() {
        Card exiledCard = new GrizzlyBears();
        Card remainingCard = new Shock();
        harness.setLibrary(player2, List.of(exiledCard, remainingCard));

        castBitterOrdeal();
        resolveSpellAndGravestorm();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    void copiesForEachPermanentPutIntoGraveyardFromBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        castBitterOrdeal();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void countsTokensPutIntoGraveyardFromBattlefield() {
        harness.setHand(player1, List.of(new EmptyTheWarrens()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> goblins = gd.playerBattlefields.get(player1.getId());
        UUID firstGoblinId = goblins.get(0).getId();
        UUID secondGoblinId = goblins.get(1).getId();
        destroyToken(firstGoblinId);
        destroyToken(secondGoblinId);

        castBitterOrdeal();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    private void castBitterOrdeal() {
        harness.setHand(player1, List.of(new BitterOrdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void resolveSpellAndGravestorm() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyToken(UUID tokenId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, tokenId);
        harness.passBothPriorities();
    }
}
