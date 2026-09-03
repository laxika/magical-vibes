package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HopelessNightmare.class, GrizzlyBears.class})
class HopelessNightmareTest extends BaseCardTest {

    @Test
    @DisplayName("Entering makes each opponent discard a card and lose 2 life")
    void entersMakesOpponentsDiscardAndLoseLife() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Entering still makes an opponent with an empty hand lose 2 life")
    void entersWithEmptyOpponentHand() {
        harness.setHand(player2, new ArrayList<>());
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Paying {2}{B} sacrifices it and triggers scry 2")
    void sacrificeAbilitySacrificesItAndScries() {
        castAndResolve();
        Permanent nightmare = findPermanent(player1, "Hopeless Nightmare");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nightmare), 0, null, null);

        assertThat(findPermanents(player1, "Hopeless Nightmare")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Hopeless Nightmare");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HopelessNightmare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
