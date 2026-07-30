package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BelltowerSphinxTest extends BaseCardTest {

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    @Test
    @DisplayName("Shock dealing 2 damage makes its controller mill 2 cards")
    void spellDamageMillsSourceController() {
        harness.addToBattlefield(player2, new BelltowerSphinx());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, library(5));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sphinxId = harness.getPermanentId(player2, "Belltower Sphinx");
        harness.castInstant(player1, 0, sphinxId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to the Sphinx

        assertThat(gd.stack).hasSize(1); // ON_DEALT_DAMAGE trigger
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(2);
        harness.assertOnBattlefield(player2, "Belltower Sphinx");
    }

    @Test
    @DisplayName("Combat damage from a blocked attacker makes the attacker's controller mill that many cards")
    void combatDamageMillsSourceController() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player2, new BelltowerSphinx()); // 2/5
        harness.setLibrary(player1, library(5));

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent sphinx = gd.playerBattlefields.get(player2.getId()).getFirst();
        sphinx.setSummoningSick(false);
        sphinx.setBlocking(true);
        sphinx.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Belltower Sphinx");
        harness.assertInGraveyard(player1, "Grizzly Bears"); // 2/2 dies to the Sphinx's 2 damage
    }

    @Test
    @DisplayName("Damage the Sphinx's own controller deals mills that controller")
    void ownControllerMillsWhenTheyDamageTheSphinx() {
        harness.addToBattlefield(player1, new BelltowerSphinx());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, library(5));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID sphinxId = harness.getPermanentId(player1, "Belltower Sphinx");
        harness.castInstant(player1, 0, sphinxId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }
}
