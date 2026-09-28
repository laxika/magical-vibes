package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CazurRuthlessStalker.class, GrizzlyBears.class})
class CazurRuthlessStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Partner with lets the target player search for Ukkima")
    void partnerWithSearchesTargetPlayersLibrary() {
        Card ukkima = namedCard("Ukkima, Stalking Shadow");
        harness.setLibrary(player2, List.of(ukkima));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new CazurRuthlessStalker());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).contains(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(ukkima);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cazur puts a +1/+1 counter on each creature you control that deals combat damage")
    void putsCountersOnCombatDamageDealers() {
        harness.addToBattlefield(player1, new CazurRuthlessStalker());
        var attacker = addCreatureReady(player1, new GrizzlyBears());

        attacker.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Card namedCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
