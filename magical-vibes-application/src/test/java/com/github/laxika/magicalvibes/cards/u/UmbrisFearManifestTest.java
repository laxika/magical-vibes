package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HorrorOfTheBrokenLands;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmbrisFearManifest.class, Forest.class, GrizzlyBears.class, HorrorOfTheBrokenLands.class})
class UmbrisFearManifestTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count cards opponents own in exile")
    void powerAndToughnessCountOpponentOwnedExiledCards() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        harness.setExile(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setExile(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, umbris)).isEqualTo(3);
    }

    @Test
    @DisplayName("It exiles each entering Nightmare or Horror's opponent's library cards through a land")
    void qualifyingCreatureEntryExilesUntilLand() {
        Permanent umbris = harness.addToBattlefieldAndReturn(player1, new UmbrisFearManifest());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Forest land = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, land, remaining));

        harness.enterBattlefieldAndReturn(player1, new HorrorOfTheBrokenLands());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gqs.getEffectivePower(gd, umbris)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enter trigger cannot target its controller")
    void enterTriggerRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new UmbrisFearManifest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
