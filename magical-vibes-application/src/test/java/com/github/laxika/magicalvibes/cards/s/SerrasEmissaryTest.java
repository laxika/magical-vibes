package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerrasEmissary.class, GrizzlyBears.class, Shock.class})
class SerrasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("As Serra's Emissary enters, it chooses a card type")
    void choosesCardTypeAsItEnters() {
        harness.setHand(player1, List.of(new SerrasEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "INSTANT");

        assertThat(findPermanent(player1, "Serra's Emissary").getChosenCardType())
                .isEqualTo(CardType.INSTANT);
    }

    @Test
    @DisplayName("You and your creatures have protection from the chosen card type")
    void protectsControllerAndOwnCreatures() {
        addReadyEmissary(player1, CardType.INSTANT);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent addReadyEmissary(Player controller, CardType chosenType) {
        Permanent emissary = new Permanent(new SerrasEmissary());
        emissary.setSummoningSick(false);
        emissary.setChosenCardType(chosenType);
        gd.playerBattlefields.get(controller.getId()).add(emissary);
        return emissary;
    }
}
