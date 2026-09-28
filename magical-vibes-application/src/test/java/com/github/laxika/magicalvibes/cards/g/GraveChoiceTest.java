package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveChoice.class, GrizzlyBears.class, HillGiant.class})
class GraveChoiceTest extends BaseCardTest {

    @Test
    void sacrificesOpponentCreatureAndConjuresSmallDuplicateWithAnyColorCasting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGraveChoice();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(duplicate));
    }

    @Test
    void doesNotConjureDuplicateWhenSacrificedCreatureIsTooExpensive() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGraveChoice();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(giant.getCard());
    }

    @Test
    void targetedOpponentChoosesTheCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castGraveChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handlePermanentChosen(player2, giant.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGraveChoice() {
        harness.setHand(player1, List.of(new GraveChoice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
