package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KraumLudevicsOpus.class, Shock.class})
class KraumLudevicsOpusTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent casts their second spell each turn")
    void drawsCardForOpponentsSecondSpell() {
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller spells do not trigger Kraum or count toward the opponent's second spell")
    void countsOnlyOpponentsSpells() {
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts an opponent's first spell even if Kraum enters afterward")
    void countsSpellsCastBeforeEntering() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draw resolves before the opponent's second spell")
    void drawsBeforeSecondSpellResolves() {
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        int lifeBeforeSecondSpell = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBeforeSecondSpell);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBeforeSecondSpell - 2);
    }

    @Test
    @DisplayName("An opponent can trigger Kraum again on the next turn")
    void spellCountResetsOnNextTurn() {
        harness.addToBattlefield(player1, new KraumLudevicsOpus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
