package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Override.class, AlphaMyr.class})
class OverrideTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when the controller cannot pay for each artifact")
    void countersWhenControllerCannotPayForArtifacts() {
        harness.addToBattlefield(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new AlphaMyr());

        AlphaMyr alphaMyr = new AlphaMyr();
        harness.castFromHand(player1, alphaMyr, "{2}");
        harness.addMana(player1, ManaColor.COLORLESS, 1); // 2 to cast, only 1 left to pay

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3); // {2}{U}
        harness.castInstant(player2, 0, alphaMyr.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alpha Myr");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can pay one for each artifact")
    void controllerCanPayForArtifacts() {
        harness.addToBattlefield(player2, new AlphaMyr());
        harness.addToBattlefield(player2, new AlphaMyr());

        AlphaMyr alphaMyr = new AlphaMyr();
        harness.castFromHand(player1, alphaMyr, "{2}");
        harness.addMana(player1, ManaColor.COLORLESS, 2); // 2 to cast, 2 to pay

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3); // {2}{U}
        harness.castInstant(player2, 0, alphaMyr.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Alpha Myr");
    }

    @Test
    @DisplayName("Counts artifacts controlled by Override's controller, including zero")
    void countsArtifactsControlledByOverrideController() {
        // The target player controls an artifact, but Override's controller controls none.
        harness.addToBattlefield(player1, new AlphaMyr());

        AlphaMyr alphaMyr = new AlphaMyr();
        harness.castFromHand(player1, alphaMyr, "{2}");

        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3); // {2}{U}
        harness.castInstant(player2, 0, alphaMyr.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // pay {0}
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpha Myr");
    }

    @Test
    @DisplayName("Can target only a spell on the stack")
    void cannotTargetPermanent() {
        AlphaMyr permanent = new AlphaMyr();
        harness.addToBattlefield(player1, permanent);

        harness.setHand(player2, List.of(new Override()));
        harness.addMana(player2, ManaColor.BLUE, 3); // {2}{U}

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
