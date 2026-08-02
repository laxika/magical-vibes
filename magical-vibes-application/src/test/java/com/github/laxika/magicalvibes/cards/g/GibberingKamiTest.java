package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GibberingKamiTest extends BaseCardTest {

    /** Wraths the board so Gibbering Kami dies, firing its soulshift trigger. */
    private void wrathToKillKami() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new GibberingKami());
        Card kami = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(kami)));

        wrathToKillKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(kami.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(kami.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(kami.getId()));
    }

    @Test
    @DisplayName("Spirits with mana value 4 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new GibberingKami());
        Card cheapSpirit = new LanternKami();
        Card expensiveSpirit = new BattlegroundGeist();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapSpirit, expensiveSpirit)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentSpirit)));

        wrathToKillKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 3 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new GibberingKami());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new BattlegroundGeist())));

        wrathToKillKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
