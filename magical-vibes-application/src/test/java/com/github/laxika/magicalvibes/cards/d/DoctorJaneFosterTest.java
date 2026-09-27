package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorJaneFoster.class, GrizzlyBears.class, HillGiant.class})
class DoctorJaneFosterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card with mana value 3 or less to hand")
    void returnsCreatureToHandWithoutLifeGain() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        castDoctorJaneFoster();
        chooseTarget(target);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Doctor Jane Foster");
    }

    @Test
    @DisplayName("Returns the target creature card to the battlefield if you gained life this turn")
    void returnsCreatureToBattlefieldWithLifeGain() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        castDoctorJaneFoster();
        chooseTarget(target);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only targets creature cards with mana value 3 or less")
    void filtersGraveyardTargets() {
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(tooExpensive, eligible));

        castDoctorJaneFoster();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    private void castDoctorJaneFoster() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DoctorJaneFoster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTarget(Card target) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
