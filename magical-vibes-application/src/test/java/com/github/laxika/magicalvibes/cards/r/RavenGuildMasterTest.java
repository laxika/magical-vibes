package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenGuildMaster.class, Forest.class, GrizzlyBears.class})
class RavenGuildMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top ten cards of the damaged player's library")
    void combatDamageExilesTopTenCards() {
        addAttackingRaven();
        Card remainingCard = new GrizzlyBears();
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new Forest());
        }
        library.add(remainingCard);
        library.add(new GrizzlyBears());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).contains(remainingCard);
        assertThat(gd.exiledCards.stream()
                .filter(entry -> player2.getId().equals(entry.ownerId()))
                .toList())
                .hasSize(10)
                .allMatch(entry -> entry.sourcePermanentId() == null);
    }

    @Test
    @DisplayName("Combat damage exiles the entire library when fewer than ten cards remain")
    void combatDamageExilesEntireSmallLibrary() {
        addAttackingRaven();
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A blocked Raven Guild Master does not exile cards")
    void noTriggerWhenBlocked() {
        addAttackingRaven();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        List<Card> library = List.of(new Forest(), new GrizzlyBears());
        harness.setLibrary(player2, library);

        resolveCombatAndTrigger();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(1));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private Permanent addAttackingRaven() {
        Permanent raven = addCreatureReady(player1, new RavenGuildMaster());
        raven.setAttacking(true);
        return raven;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
