package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowOfDoubt.class, DiabolicTutor.class, GrizzlyBears.class, Plains.class, Swamp.class})
class ShadowOfDoubtTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents library searches for the turn and draws a card")
    void preventsSearchesAndDraws() {
        Card drawn = new Plains();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new ShadowOfDoubt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersCantSearchLibrariesThisTurn).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        harness.setLibrary(player2, List.of(new Swamp(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new DiabolicTutor()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The restriction expires during end-of-turn cleanup")
    void restrictionExpiresAtEndOfTurn() {
        gd.playersCantSearchLibrariesThisTurn = true;

        new TurnCleanupService(null, null).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersCantSearchLibrariesThisTurn).isFalse();
    }
}
