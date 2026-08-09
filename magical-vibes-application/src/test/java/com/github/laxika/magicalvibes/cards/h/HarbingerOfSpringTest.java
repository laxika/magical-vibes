package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
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

class HarbingerOfSpringTest extends BaseCardTest {

    private void killHarbinger() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift returns a targeted Spirit with mana value 4 or less from your graveyard to your hand")
    void soulshiftReturnsEligibleSpirit() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        Card eligible = new LanternKami();
        Card nonSpirit = new GrizzlyBears();
        Card tooExpensive = new SpiritOfTheNight();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(eligible, nonSpirit, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killHarbinger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spirit of the Night");
        harness.assertInGraveyard(player2, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        Card eligible = new LanternKami();
        harness.setGraveyard(player1, List.of(eligible));

        killHarbinger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Soulshift does not present a choice without a legal target")
    void noEligibleTargetMeansNoChoice() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new SpiritOfTheNight()));

        killHarbinger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
