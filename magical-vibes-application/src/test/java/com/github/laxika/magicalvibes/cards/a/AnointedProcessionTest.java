package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnointedProcessionTest extends BaseCardTest {

    // ===== Doubles ETB token creation =====

    @Test
    @DisplayName("Blade Splicer ETB creates 2 Golem tokens instead of 1 with Anointed Procession")
    void doublesEtbTokenCreation() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    // ===== Does not affect opponent's tokens =====

    @Test
    @DisplayName("Anointed Procession does not double tokens for the opponent")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> opponentTokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(opponentTokens).hasSize(1);
    }

    // ===== Multiple copies stack multiplicatively =====

    @Test
    @DisplayName("Two Anointed Processions quadruple tokens (1 -> 4)")
    void twoProcessionsQuadrupleTokens() {
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.addToBattlefield(player1, new AnointedProcession());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(4);
    }

    // ===== Without Anointed Procession, normal token count =====

    @Test
    @DisplayName("Without Anointed Procession, Blade Splicer creates exactly 1 token")
    void noDoublingWithoutProcession() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(1);
    }
}
