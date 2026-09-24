package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BriarknitKami;
import com.github.laxika.magicalvibes.cards.d.DeathknellKami;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.cards.m.MasumaroFirstToLive;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
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

@CardUsed({NightsoilKami.class, MirenTheMoaningWell.class, DeathknellKami.class,
        BriarknitKami.class, MasumaroFirstToLive.class, HandOfHonor.class})
class NightsoilKamiTest extends BaseCardTest {

    private void mirenToKillNightsoilKami(Permanent nightsoil) {
        harness.addToBattlefield(player1, new MirenTheMoaningWell());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 1, null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, nightsoil.getId());
        }
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulshift 5 returns a targeted Spirit with mana value 5 or less from your graveyard to your hand")
    void deathReturnsCheapSpiritToHand() {
        Permanent nightsoil = harness.addToBattlefieldAndReturn(player1, new NightsoilKami());
        Card spirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(spirit));

        mirenToKillNightsoilKami(nightsoil);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Only your Spirits with mana value 5 or less are legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        Permanent nightsoil = harness.addToBattlefieldAndReturn(player1, new NightsoilKami());
        Card cheapSpirit = new DeathknellKami();
        Card boundarySpirit = new BriarknitKami();
        Card expensiveSpirit = new MasumaroFirstToLive();
        Card nonSpirit = new HandOfHonor();
        Card opponentSpirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        mirenToKillNightsoilKami(nightsoil);

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(
                expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftMayBeDeclined() {
        Permanent nightsoil = harness.addToBattlefieldAndReturn(player1, new NightsoilKami());
        Card spirit = new DeathknellKami();
        harness.setGraveyard(player1, List.of(spirit));

        mirenToKillNightsoilKami(nightsoil);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("With no Spirit with mana value 5 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        Permanent nightsoil = harness.addToBattlefieldAndReturn(player1, new NightsoilKami());
        harness.setGraveyard(player1, List.of(new HandOfHonor()));

        mirenToKillNightsoilKami(nightsoil);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
