package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhammyBurn.class, Forest.class})
class WhammyBurnTest extends BaseCardTest {

    @Test
    @DisplayName("Continuing reveals through the whammy deck and stops on an Island without damage")
    void continuingStopsOnIslandWithoutDamage() {
        harness.setHand(player1, List.of(new WhammyBurn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        while (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Stopping after a non-Island deals one damage for the revealed card")
    void stoppingDealsDamageForRevealedCards() {
        boolean stopped = false;
        for (int attempt = 0; attempt < 50 && !stopped; attempt++) {
            harness.setHand(player1, List.of(new WhammyBurn()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, false);
                stopped = true;
            }
        }

        assertThat(stopped).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Only creatures, planeswalkers, battles, and players can be targeted")
    void onlyAnyTargetsCanBeTargeted() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new WhammyBurn()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
