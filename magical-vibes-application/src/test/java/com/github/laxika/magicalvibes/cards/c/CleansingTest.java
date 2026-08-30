package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cleansing.class, Forest.class})
class CleansingTest extends BaseCardTest {

    @Test
    @DisplayName("Any player can pay 1 life to keep the land")
    void anyPlayerCanPayToKeepLand() {
        harness.addToBattlefield(player2, new Forest());
        int life2 = gd.getLife(player2.getId());
        castCleansing();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("The land is destroyed when no player pays")
    void destroysLandWhenNoPlayerPays() {
        harness.addToBattlefield(player2, new Forest());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Each land has an independent payment")
    void paymentsAreIndependentPerLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        int life1 = gd.getLife(player1.getId());
        castCleansing();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player2, "Forest")).hasSize(1);
        harness.assertLife(player1, life1 - 1);
    }

    @Test
    @DisplayName("Does nothing when there are no lands")
    void noLandsNoPrompt() {
        castCleansing();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCleansing() {
        harness.setHand(player1, List.of(new Cleansing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
