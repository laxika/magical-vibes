package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HundredTalonKami;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.m.MoaningSpirit;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForkedBranchGaramiTest extends BaseCardTest {

    private void killGarami() {
        harness.addToBattlefield(player1, new ForkedBranchGarami());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Death trigger returns up to two target Spirits with mana value 4 or less")
    void returnsUpToTwoEligibleSpirits() {
        Card firstSpirit = new LanternKami();
        Card secondSpirit = new MoaningSpirit();
        Card nonSpirit = new GrizzlyBears();
        Card expensiveSpirit = new HundredTalonKami();
        harness.setGraveyard(player1, List.of(firstSpirit, secondSpirit, nonSpirit, expensiveSpirit));

        killGarami();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstSpirit.getId(), secondSpirit.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstSpirit.getId(), secondSpirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lantern Kami");
        harness.assertInHand(player1, "Moaning Spirit");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hundred-Talon Kami");
    }

    @Test
    @DisplayName("Death trigger can return no Spirit cards")
    void mayReturnNoSpirits() {
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killGarami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Death trigger has no choice when no eligible Spirit exists")
    void noEligibleSpiritMeansNoChoice() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HundredTalonKami()));

        killGarami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
