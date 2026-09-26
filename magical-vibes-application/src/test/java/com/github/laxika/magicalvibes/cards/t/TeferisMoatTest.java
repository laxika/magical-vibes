package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlazingSpecter;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeferisMoat.class, YavimayaBarbarian.class, BlazingSpecter.class, NomadicElf.class})
class TeferisMoatTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Teferi's Moat asks its controller to choose a color")
    void resolvingAsksForColor() {
        harness.castFromHand(player1, new TeferisMoat(), "{3}{W}{U}");
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");

        harness.handleListChoice(player1, "RED");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A chosen-color creature without flying cannot attack the Moat controller")
    void chosenColorGroundCreatureCannotAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, new YavimayaBarbarian());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A flying creature of the chosen color can attack the Moat controller")
    void chosenColorFlyerCanAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, new BlazingSpecter());

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("A creature of another color can attack the Moat controller")
    void differentColorCreatureCanAttackController() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addCreatureReady(player1, new NomadicElf());

        declareAttackers(player1, List.of(0));
    }

    private void addMoatWithChosenColor(Player controller, CardColor color) {
        Permanent moat = harness.addToBattlefieldAndReturn(controller, new TeferisMoat());
        moat.setChosenColor(color);
    }
}
