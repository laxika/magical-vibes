package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObstinateBaloth.class, Distress.class, GrizzlyBears.class, MindRot.class, Sift.class})
class ObstinateBalothTest extends BaseCardTest {

    // ===== ETB life gain when cast normally =====

    @Test
    @DisplayName("Gains 4 life when entering the battlefield via casting")
    void gains4LifeWhenCast() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ObstinateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Is on the battlefield after casting")
    void onBattlefieldAfterCasting() {
        harness.setHand(player1, List.of(new ObstinateBaloth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Obstinate Baloth");
    }

    // ===== Replacement effect — opponent forces discard via Distress (revealed hand choice) =====

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Distress")
    void entersBattlefieldWhenDiscardedByOpponentViaDistress() {
        harness.setHand(player2, new ArrayList<>(List.of(new ObstinateBaloth())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player1 chooses Obstinate Baloth from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Obstinate Baloth should be on player2's battlefield
        harness.assertOnBattlefield(player2, "Obstinate Baloth");

        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
    }

    @Test
    @DisplayName("Gains 4 life from ETB when entering via Distress discard replacement")
    void gains4LifeWhenEnteringViaDistressDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ObstinateBaloth())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player1 chooses Obstinate Baloth from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Resolve ETB trigger
        harness.passBothPriorities();

        // Player2 should have gained 4 life from ETB
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    // ===== Replacement effect — opponent forces discard via Mind Rot (discard choice) =====

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Mind Rot")
    void entersBattlefieldWhenDiscardedByOpponentViaMindRot() {
        harness.setHand(player2, new ArrayList<>(List.of(new ObstinateBaloth(), new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player2 discards Obstinate Baloth first
        harness.handleCardChosen(player2, 0);

        // Obstinate Baloth should be on player2's battlefield
        harness.assertOnBattlefield(player2, "Obstinate Baloth");

        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
    }

    @Test
    @DisplayName("Gains 4 life from ETB when entering via Mind Rot discard replacement")
    void gains4LifeWhenEnteringViaMindRotDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new ObstinateBaloth(), new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player2 discards Obstinate Baloth first
        harness.handleCardChosen(player2, 0);

        // Player2 discards Grizzly Bears second
        harness.handleCardChosen(player2, 0);

        // Resolve ETB trigger
        harness.passBothPriorities();

        // Player2 should have gained 4 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    // ===== No replacement on self-discard =====

    @Test
    @DisplayName("Does NOT enter battlefield when controller discards it themselves via Sift")
    void doesNotEnterBattlefieldOnSelfDiscard() {
        harness.setLife(player1, 20);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Sift(), new ObstinateBaloth()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0); // Resolve Sift — draws 3, prompts for discard

        // Player1 discards Obstinate Baloth
        harness.handleCardChosen(player1, 0);

        // Should be in graveyard, NOT on battlefield
        harness.assertInGraveyard(player1, "Obstinate Baloth");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");

        // Life should not change (no ETB)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Hand is empty after replacement =====

    @Test
    @DisplayName("Player's hand is empty after Obstinate Baloth enters via Distress")
    void handEmptyAfterDistressReplacement() {
        harness.setHand(player2, new ArrayList<>(List.of(new ObstinateBaloth())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discard replacement does not gain life until the ETB trigger resolves")
    void discardReplacementQueuesLifeGain() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ObstinateBaloth()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 24);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Both Baloths enter and each gains life when discarded to opponent's Mind Rot")
    void twoBalothsDiscardedToMindRot() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ObstinateBaloth(), new ObstinateBaloth()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof ObstinateBaloth)
                .hasSize(2);
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 28);
    }

    @Test
    @DisplayName("Mind Rot targeting its own controller does not replace Baloth's discard")
    void selfTargetedMindRotDoesNotReplaceDiscard() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MindRot(), new ObstinateBaloth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Obstinate Baloth");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Obstinate Baloth");
        harness.assertLife(player1, 20);
    }
}
