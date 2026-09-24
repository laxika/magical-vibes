package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DeathknellKami;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.cards.s.SekkiSeasonsGuide;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PromisedKannushi.class, DeathknellKami.class, HandOfHonor.class,
        MirenTheMoaningWell.class, SekkiSeasonsGuide.class})
class PromisedKannushiTest extends BaseCardTest {

    private void sacrificeKannushi(Permanent kannushi) {
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 1, null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, kannushi.getId());
        }
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 7 returns a targeted Spirit with mana value 7 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new PromisedKannushi());
        Card spirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(spirit));

        sacrificeKannushi(kannushi);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathknell Kami");
        harness.assertNotInGraveyard(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Only your Spirits with mana value 7 or less are legal targets")
    void soulshiftFiltersTargets() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new PromisedKannushi());
        Card eligibleSpirit = new DeathknellKami();
        Card expensiveSpirit = new SekkiSeasonsGuide();
        Card nonSpirit = new HandOfHonor();
        Card opponentSpirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(eligibleSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        sacrificeKannushi(kannushi);

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligibleSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftMayBeDeclined() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new PromisedKannushi());
        Card spirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(spirit));

        sacrificeKannushi(kannushi);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deathknell Kami");
        harness.assertNotInHand(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("With no legal Spirit in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new PromisedKannushi());
        harness.setGraveyard(player1, List.of(new HandOfHonor(), new SekkiSeasonsGuide()));

        sacrificeKannushi(kannushi);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
