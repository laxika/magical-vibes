package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThousandLeggedKami.class, LanternKami.class, RendSpirit.class, SakuraTribeElder.class})
class ThousandLeggedKamiTest extends BaseCardTest {

    /** Destroys Thousand-legged Kami so its soulshift trigger fires. */
    private void destroyKami() {
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, findPermanent(player1, "Thousand-legged Kami").getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 7 returns a targeted Spirit with mana value 7 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new ThousandLeggedKami());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        destroyKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Expensive Spirits, non-Spirits, and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new ThousandLeggedKami());
        Card cheapSpirit = new LanternKami();
        Card expensiveSpirit = new ThousandLeggedKami();
        Card nonSpirit = new SakuraTribeElder();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        destroyKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift can be declined after choosing its target")
    void mayDeclineSoulshiftAtResolution() {
        harness.addToBattlefield(player1, new ThousandLeggedKami());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, List.of(spirit));

        destroyKami();

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("With no Spirit with mana value 7 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new ThousandLeggedKami());
        harness.setGraveyard(player1, List.of(new ThousandLeggedKami()));

        destroyKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
