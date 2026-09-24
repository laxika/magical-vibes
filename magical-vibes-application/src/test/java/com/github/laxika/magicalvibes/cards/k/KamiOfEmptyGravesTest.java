package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.n.NikkoOnna;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfEmptyGraves.class, KikusShadow.class, NikkoOnna.class,
        KamiOfTheTendedGarden.class, GhostLitRedeemer.class, KitsuneBonesetter.class})
class KamiOfEmptyGravesTest extends BaseCardTest {

    private void kikuToKillKami() {
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0,
                harness.getPermanentId(player1, "Kami of Empty Graves"));
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new KamiOfEmptyGraves());
        Card spirit = new NikkoOnna();
        harness.setGraveyard(player1, List.of(spirit));

        kikuToKillKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Spirits with mana value 4 or greater, non-Spirits, and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new KamiOfEmptyGraves());
        Card cheapSpirit = new NikkoOnna();
        Card expensiveSpirit = new KamiOfTheTendedGarden();
        Card nonSpirit = new KitsuneBonesetter();
        Card opponentSpirit = new GhostLitRedeemer();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        kikuToKillKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(
                expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new KamiOfEmptyGraves());
        Card spirit = new NikkoOnna();
        harness.setGraveyard(player1, List.of(spirit));

        kikuToKillKami();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("With no Spirit with mana value 3 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new KamiOfEmptyGraves());
        harness.setGraveyard(player1, List.of(new KamiOfTheTendedGarden(), new KitsuneBonesetter()));

        kikuToKillKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
