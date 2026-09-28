package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromancersFamiliar.class, GrizzlyBears.class})
class NecromancersFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink only while its controller has no cards in hand")
    void lifelinkRequiresEmptyHand() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NecromancersFamiliar());

        harness.setHand(player1, List.of());
        assertThat(gqs.hasKeyword(gd, familiar, Keyword.LIFELINK)).isTrue();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThat(gqs.hasKeyword(gd, familiar, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Pays black mana and a discard to gain indestructible and tap")
    void discardsForIndestructibleAndTap() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NecromancersFamiliar());
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gqs.hasKeyword(gd, familiar, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new NecromancersFamiliar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, familiar, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
