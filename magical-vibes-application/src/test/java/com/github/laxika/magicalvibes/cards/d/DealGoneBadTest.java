package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DealGoneBad.class, GrizzlyBears.class})
class DealGoneBadTest extends BaseCardTest {

    @Test
    @DisplayName("gives a creature -3/-3 and mills three cards from a target player's library")
    void resolvesBothEffects() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, bearCard);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        cast(bear.getId(), player2.getId());

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("the -3/-3 effect expires at cleanup")
    void debuffExpiresAtCleanup() {
        GrizzlyBears bearCard = new GrizzlyBears();
        bearCard.setPower(5);
        bearCard.setToughness(5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, bearCard);

        cast(bear.getId(), player2.getId());

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void requiresCreatureAndPlayerTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID creatureTargetId, java.util.UUID playerTargetId) {
        harness.setHand(player1, List.of(new DealGoneBad()));
        addMana();
        harness.castInstant(player1, 0, List.of(creatureTargetId, playerTargetId));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
