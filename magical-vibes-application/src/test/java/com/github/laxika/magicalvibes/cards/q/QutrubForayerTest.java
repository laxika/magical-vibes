package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QutrubForayer.class, Forest.class, GrizzlyBears.class})
class QutrubForayerTest extends BaseCardTest {

    @Test
    void destroysCreatureThatWasDealtDamageThisTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        cast(0, targetId);
        resolveCreatureAndEtb();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroyModeRejectsCreatureThatWasNotDealtDamageThisTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        assertThatThrownBy(() -> cast(0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    void exilesUpToTwoCardsFromOneGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card left = new Forest();
        harness.setGraveyard(player2, List.of(first, second, left));

        cast(1, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(left);
    }

    private void cast(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new QutrubForayer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
