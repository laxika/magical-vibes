package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.InameAsOne;
import com.github.laxika.magicalvibes.cards.k.KikusShadow;
import com.github.laxika.magicalvibes.cards.k.KamiOfEmptyGraves;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        NightsoilKami.class,
        KikusShadow.class,
        KamiOfEmptyGraves.class,
        InameAsOne.class,
        PithingNeedle.class
})
class NightsoilKamiTest extends BaseCardTest {

    private void kikuShadowToKillNightsoilKami() {
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Nightsoil Kami"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 5 returns a targeted Spirit with mana value 5 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new NightsoilKami());
        Card spirit = new KamiOfEmptyGraves();
        harness.setGraveyard(player1, List.of(spirit));

        kikuShadowToKillNightsoilKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Spirits with mana value 6 or greater and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new NightsoilKami());
        Card cheapSpirit = new KamiOfEmptyGraves();
        Card expensiveSpirit = new InameAsOne();
        Card opponentSpirit = new KamiOfEmptyGraves();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        kikuShadowToKillNightsoilKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 5 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new NightsoilKami());
        harness.setGraveyard(player1, List.of(new PithingNeedle()));

        kikuShadowToKillNightsoilKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
