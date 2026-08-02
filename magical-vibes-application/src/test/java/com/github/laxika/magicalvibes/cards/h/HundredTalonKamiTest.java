package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoaningSpirit;
import com.github.laxika.magicalvibes.cards.s.SpiritOfTheNight;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HundredTalonKamiTest extends BaseCardTest {

    private void killKami() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift targets a Spirit card with mana value 4 or less from its controller's graveyard")
    void soulshiftReturnsEligibleSpirit() {
        harness.addToBattlefield(player1, new HundredTalonKami());
        Card eligible = new MoaningSpirit();
        Card nonSpirit = new GrizzlyBears();
        Card tooExpensive = new SpiritOfTheNight();
        Card opponentSpirit = new MoaningSpirit();
        harness.setGraveyard(player1, List.of(eligible, nonSpirit, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Moaning Spirit");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spirit of the Night");
        harness.assertInGraveyard(player2, "Moaning Spirit");
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new HundredTalonKami());
        Card eligible = new MoaningSpirit();
        harness.setGraveyard(player1, List.of(eligible));

        killKami();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Moaning Spirit");
        harness.assertNotInHand(player1, "Moaning Spirit");
    }

    @Test
    @DisplayName("Soulshift does not trigger a graveyard choice without a legal target")
    void noEligibleTargetMeansNoChoice() {
        harness.addToBattlefield(player1, new HundredTalonKami());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new SpiritOfTheNight()));

        killKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
