package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntoTheFaeCourt.class, AirElemental.class, GrizzlyBears.class})
class IntoTheFaeCourtTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and creates a Faerie token")
    void drawsThreeCardsAndCreatesFaerieToken() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        Permanent faerie = castIntoTheFaeCourt();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
        assertThat(faerie.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, faerie, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The Faerie token can block a creature with flying")
    void faerieCanBlockFlyingCreature() {
        Permanent faerie = castIntoTheFaeCourt();
        faerie.setSummoningSick(false);
        addCreatureReady(player2, new AirElemental()).setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(faerie.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The Faerie token cannot block a creature without flying")
    void faerieCannotBlockNonFlyingCreature() {
        Permanent faerie = castIntoTheFaeCourt();
        faerie.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears()).setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    private Permanent castIntoTheFaeCourt() {
        harness.setHand(player1, List.of(new IntoTheFaeCourt()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
