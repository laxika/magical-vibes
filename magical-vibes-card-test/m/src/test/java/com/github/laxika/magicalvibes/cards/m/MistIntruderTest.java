package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.e.EldraziSkyspawner;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MistIntruder.class, EldraziSkyspawner.class})
class MistIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the top card of the damaged player's library")
    void combatDamageExilesTopCard() {
        Permanent intruder = addAttackingIntruder(player1);
        EldraziSkyspawner topCard = new EldraziSkyspawner();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.sourcePermanentId()).isNull();
        assertThat(gd.getCardsExiledByPermanent(intruder.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled when the damaged player's library is empty")
    void noExileWhenLibraryEmpty() {
        addAttackingIntruder(player1);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Ingest exiles exactly the top card and leaves both remaining libraries intact")
    void exilesOnlyTopCard() {
        addAttackingIntruder(player1);
        EldraziSkyspawner topCard = new EldraziSkyspawner();
        MistIntruder nextCard = new MistIntruder();
        EldraziSkyspawner ownCard = new EldraziSkyspawner();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of(ownCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's Mist Intruder exiles from the player it damaged")
    void opponentIntruderExilesDamagedPlayersCard() {
        addAttackingIntruder(player2);
        EldraziSkyspawner topCard = new EldraziSkyspawner();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Ingest resolves even after Mist Intruder leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent intruder = addAttackingIntruder(player1);
        EldraziSkyspawner topCard = new EldraziSkyspawner();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        gd.playerBattlefields.get(player1.getId()).remove(intruder);
        gd.playerGraveyards.get(player1.getId()).add(intruder.getCard());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
    }

    private Permanent addAttackingIntruder(Player player) {
        Permanent intruder = addCreatureReady(player, new MistIntruder());
        intruder.setAttacking(true);
        return intruder;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
