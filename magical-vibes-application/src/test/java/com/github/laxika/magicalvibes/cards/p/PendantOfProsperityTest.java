package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PendantOfProsperity.class, Forest.class, GrizzlyBears.class})
class PendantOfProsperityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters under an opponent's control")
    void entersUnderOpponentsControl() {
        harness.setHand(player1, List.of(new PendantOfProsperity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pendant of Prosperity");
        harness.assertOnBattlefield(player2, "Pendant of Prosperity");
    }

    @Test
    @DisplayName("Lets the controller and owner draw and put lands from their hands onto the battlefield")
    void controllerAndOwnerResolveTheirChoices() {
        PendantOfProsperity pendant = new PendantOfProsperity();
        pendant.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, pendant);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Card ownerDraw = new GrizzlyBears();
        Card controllerDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownerDraw));
        harness.setLibrary(player2, List.of(controllerDraw));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).contains(ownerDraw);
        assertThat(gd.playerHands.get(player2.getId())).contains(controllerDraw);
    }
}
