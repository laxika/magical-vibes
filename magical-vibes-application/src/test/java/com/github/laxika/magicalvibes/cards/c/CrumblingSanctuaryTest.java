package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrumblingSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage is replaced by exiling cards from the damaged player's library")
    void noncombatDamageExilesLibraryCards() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The replacement affects players other than the artifact's controller")
    void replacementAffectsOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Damage has no effect when the damaged player's library is empty")
    void emptyLibraryTakesNoDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Combat damage is replaced by exiling cards from the defending player's library")
    void combatDamageExilesLibraryCards() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CrumblingSanctuary());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }
}
