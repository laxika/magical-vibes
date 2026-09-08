package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutridImp.class, Forest.class, GrizzlyBears.class})
class PutridImpTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card gives Putrid Imp flying until end of turn")
    void discardCardGrantsFlyingUntilEndOfTurn() {
        Permanent imp = addReadyImp(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isTrue();
        harness.assertInGraveyard(player1, "Forest");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Threshold gives Putrid Imp +1/+1 and prevents it from blocking")
    void thresholdBoostsImpAndPreventsBlocking() {
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent imp = addReadyImp(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, imp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, imp)).isEqualTo(2);

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Putrid Imp can block while its controller has fewer than seven graveyard cards")
    void canBlockBelowThreshold() {
        harness.setGraveyard(player2, graveyardCards(6));
        addReadyImp(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Putrid Imp cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyImp(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a card");
    }

    private Permanent addReadyImp(Player player) {
        return addCreatureReady(player, new PutridImp());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
