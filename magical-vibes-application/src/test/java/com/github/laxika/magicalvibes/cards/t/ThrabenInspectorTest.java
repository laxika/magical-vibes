package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThrabenInspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Thraben Inspector enters, one Clue token is created")
    void etbCreatesOneClueToken() {
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB (investigate)

        List<Permanent> clues = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clue"))
                .toList();
        assertThat(clues).hasSize(1);
        Permanent clue = clues.getFirst();
        assertThat(clue.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(clue.getCard().getSubtypes()).contains(CardSubtype.CLUE);
        assertThat(clue.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a Clue for {2} draws a card")
    void clueSacrificeDrawsCard() {
        harness.setHand(player1, List.of(new ThrabenInspector()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clue = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Clue"))
                .findFirst().orElseThrow();
        int clueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(clue);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, clueIdx, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Clue"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
