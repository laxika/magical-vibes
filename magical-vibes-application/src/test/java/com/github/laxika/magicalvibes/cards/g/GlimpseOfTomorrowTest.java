package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlimpseOfTomorrow.class, GrizzlyBears.class, Pacifism.class, Shock.class})
class GlimpseOfTomorrowTest extends BaseCardTest {

    @Test
    void suspendShufflesOwnPermanentsAndReturnsNonAurasBeforeAuras() {
        GlimpseOfTomorrow glimpse = new GlimpseOfTomorrow();
        Card creatureCard = new GrizzlyBears();
        Card auraCard = new Pacifism();
        Card instantCard = new Shock();
        Card opponentLibraryCard = new Shock();

        harness.setHand(player1, List.of(glimpse));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player1, token("First token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Second token", CardType.CREATURE));
        harness.addToBattlefield(player1, token("Third token", CardType.CREATURE));
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, token("Opponent land", CardType.LAND));
        harness.setLibrary(player1, List.of(auraCard, creatureCard, instantCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(glimpse);
        assertThat(gd.exiledCardTimeCounters).containsEntry(glimpse.getId(), 3);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentLand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == creatureCard)
                .findFirst()
                .orElseThrow();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == auraCard)
                .findFirst()
                .orElseThrow();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instantCard);
    }

    private Card token(String name, CardType type) {
        Card token = new Card();
        token.setName(name);
        token.setType(type);
        token.setToken(true);
        if (type == CardType.CREATURE) {
            token.setPower(1);
            token.setToughness(1);
        }
        return token;
    }
}
