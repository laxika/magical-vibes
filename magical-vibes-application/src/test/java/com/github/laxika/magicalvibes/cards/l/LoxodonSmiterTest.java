package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonSmiterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        LoxodonSmiter smiter = new LoxodonSmiter();
        harness.setHand(player1, List.of(smiter));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, smiter.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Smiter");
        harness.assertNotInGraveyard(player1, "Loxodon Smiter");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Distress")
    void entersBattlefieldWhenDiscardedByOpponentViaDistress() {
        harness.setHand(player2, new ArrayList<>(List.of(new LoxodonSmiter())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Mind Rot")
    void entersBattlefieldWhenDiscardedByOpponentViaMindRot() {
        harness.setHand(player2, new ArrayList<>(List.of(new LoxodonSmiter(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Does NOT enter battlefield when controller discards it themselves")
    void doesNotEnterBattlefieldOnSelfDiscard() {
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift(), new LoxodonSmiter()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Loxodon Smiter");
        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
    }

    @Test
    @DisplayName("Hand is empty after Smiter enters via Distress")
    void handEmptyAfterDistressReplacement() {
        harness.setHand(player2, new ArrayList<>(List.of(new LoxodonSmiter())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
