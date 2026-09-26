package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HideousLaughter;
import com.github.laxika.magicalvibes.cards.h.HikariTwilightGuardian;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfLunacy.class, HideousLaughter.class, HikariTwilightGuardian.class,
        KokushoTheEveningStar.class, LanternKami.class})
class KamiOfLunacyTest extends BaseCardTest {

    /** Gives Kami of Lunacy lethal -2/-2, firing its soulshift trigger. */
    private void killKami() {
        harness.castFromHand(player1, new HideousLaughter(), "{2}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 5 returns a targeted Spirit with mana value 5 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new KamiOfLunacy());
        Card kami = new LanternKami();
        harness.setGraveyard(player1, List.of(kami));

        killKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(kami.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kami.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(kami.getId()));
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new KamiOfLunacy());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        killKami();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lantern Kami");
        harness.assertNotInHand(player1, "Lantern Kami");
    }

    @Test
    @DisplayName("Spirits with mana value 6 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new KamiOfLunacy());
        Card cheapSpirit = new HikariTwilightGuardian();
        Card expensiveSpirit = new KokushoTheEveningStar();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 5 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new KamiOfLunacy());
        harness.setGraveyard(player1, List.of(new KokushoTheEveningStar()));

        killKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
