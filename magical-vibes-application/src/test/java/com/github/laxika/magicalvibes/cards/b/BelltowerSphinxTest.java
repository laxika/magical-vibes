package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelltowerSphinx.class, BorosGuildmage.class, Char.class, Forest.class})
class BelltowerSphinxTest extends BaseCardTest {

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    @Test
    @DisplayName("Char dealing 4 damage makes its controller mill 4 cards")
    void spellDamageMillsSourceController() {
        harness.addToBattlefield(player2, new BelltowerSphinx());
        harness.setHand(player1, List.of(new Char()));
        harness.setLibrary(player1, library(5));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID sphinxId = harness.getPermanentId(player2, "Belltower Sphinx");
        harness.castAndResolveInstant(player1, 0, sphinxId); // Resolve Char — 4 damage to the Sphinx

        assertThat(gd.stack).hasSize(1); // ON_DEALT_DAMAGE trigger
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(4);
        harness.assertOnBattlefield(player2, "Belltower Sphinx");
    }

    @Test
    @DisplayName("Combat damage from a blocked attacker makes the attacker's controller mill that many cards")
    void combatDamageMillsSourceController() {
        addCreatureReady(player1, new BorosGuildmage()); // 2/2
        addCreatureReady(player2, new BelltowerSphinx()); // 2/5
        harness.setLibrary(player1, library(5));

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Belltower Sphinx");
        harness.assertInGraveyard(player1, "Boros Guildmage"); // 2/2 dies to the Sphinx's 2 damage
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Belltower Sphinx")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new BelltowerSphinx());
        addCreatureReady(player2, new BorosGuildmage());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage the Sphinx's own controller deals mills that controller")
    void ownControllerMillsWhenTheyDamageTheSphinx() {
        harness.addToBattlefield(player1, new BelltowerSphinx());
        harness.setHand(player1, List.of(new Char()));
        harness.setLibrary(player1, library(5));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID sphinxId = harness.getPermanentId(player1, "Belltower Sphinx");
        harness.castAndResolveInstant(player1, 0, sphinxId);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
