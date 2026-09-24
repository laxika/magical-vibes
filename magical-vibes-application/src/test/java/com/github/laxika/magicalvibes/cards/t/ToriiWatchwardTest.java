package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KikusShadow;
import com.github.laxika.magicalvibes.cards.k.KamiOfEmptyGraves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToriiWatchward.class, KamiOfEmptyGraves.class, KikusShadow.class})
class ToriiWatchwardTest extends BaseCardTest {

    private void kikuToKillWatchward() {
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Torii Watchward"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 4 returns a targeted Spirit with mana value 4 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new ToriiWatchward());
        Card spirit = new KamiOfEmptyGraves();
        harness.setGraveyard(player1, List.of(spirit));

        kikuToKillWatchward();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Spirits with mana value 5 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new ToriiWatchward());
        Card cheapSpirit = new KamiOfEmptyGraves();
        Card expensiveSpirit = new ToriiWatchward();
        Card nonSpirit = new KikusShadow();
        Card opponentSpirit = new KamiOfEmptyGraves();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        kikuToKillWatchward();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(
                expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new ToriiWatchward());
        Card spirit = new KamiOfEmptyGraves();
        harness.setGraveyard(player1, List.of(spirit));

        kikuToKillWatchward();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("With no Spirit with mana value 4 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new ToriiWatchward());
        harness.setGraveyard(player1, List.of(new ToriiWatchward()));

        kikuToKillWatchward();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
