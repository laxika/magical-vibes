package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagingGoblinoids.class})
class RagingGoblinoidsTest extends BaseCardTest {

    @Test
    @DisplayName("Mayhem casts Raging Goblinoids from the graveyard after it was discarded this turn")
    void mayhemCastsAfterDiscarding() {
        RagingGoblinoids card = new RagingGoblinoids();
        harness.setGraveyard(player1, List.of(card));
        gd.cardsDiscardedOrCycledThisTurn.put(player1.getId(), new HashSet<>(Set.of(card.getId())));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Mayhem cannot cast Raging Goblinoids from the graveyard before it was discarded")
    void mayhemRequiresDiscardThisTurn() {
        harness.setGraveyard(player1, List.of(new RagingGoblinoids()));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
