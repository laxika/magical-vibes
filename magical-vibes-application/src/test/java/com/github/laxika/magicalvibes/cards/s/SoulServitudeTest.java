package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulServitude.class, Forest.class, GrizzlyBears.class})
class SoulServitudeTest extends BaseCardTest {

    @Test
    void targetPlayerSacrificesAnyNontokenCreatureAndDiscardConjuresItsDuplicate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), new Forest())));
        cast();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo(bears.getCard().getName());
        assertThat(duplicate.getId()).isNotEqualTo(bears.getCard().getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());
    }

    @Test
    void decliningDiscardDoesNotConjure() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Forest forest = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), forest)));
        cast();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void canTargetTheController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, new ArrayList<>(List.of(new SoulServitude(), new Forest())));
        cast(player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        harness.handleMayAbilityChosen(player1, false);
    }

    private void cast() {
        cast(player2.getId());
    }

    private void cast(java.util.UUID targetPlayerId) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
