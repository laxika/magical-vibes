package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilentSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers an enchantment card from your graveyard")
    void attackOffersEnchantmentFromOwnGraveyard() {
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));
        addReadySilentSentinel();

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(insight.getId());

        harness.handleMultipleCardsChosen(player1, List.of(insight.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Insight");
        harness.assertNotInGraveyard(player1, "Insight");
    }

    @Test
    @DisplayName("Nonenchantment cards are not legal attack targets")
    void attackDoesNotOfferNonenchantmentCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addReadySilentSentinel();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the enchantment in the graveyard")
    void decliningAttackTriggerLeavesGraveyardUntouched() {
        Card insight = new Insight();
        harness.setGraveyard(player1, List.of(insight));
        addReadySilentSentinel();

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(insight.getId());
        harness.assertNotOnBattlefield(player1, "Insight");
    }

    private Permanent addReadySilentSentinel() {
        Permanent sentinel = new Permanent(new SilentSentinel());
        sentinel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentinel);
        return sentinel;
    }
}
