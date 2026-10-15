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
    @DisplayName("The color chosen as Teferi's Moat enters controls its attack restriction")
    void chosenColorFromEntryRestrictsGroundCreature() {
        harness.castFromHand(player1, new TeferisMoat(), "{3}{W}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        addCreatureReady(player2, new YavimayaBarbarian());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
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

    @Test
    @DisplayName("Entering without being cast still requires a color choice")
    void enteringWithoutCastingChoosesColor() {
        TeferisMoat moat = new TeferisMoat();
        harness.addToBattlefield(player2, moat);
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .handleCreatureEnteredBattlefield(gd, player2.getId(), moat, null, false));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GREEN");
        assertThat(gd.interaction.activeInteraction()).isNull();

        addCreatureReady(player1, new NomadicElf());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Either color of a multicolored ground creature can be chosen to prevent its attack")
    void secondColorOfMulticoloredCreatureIsRestricted() {
        addMoatWithChosenColor(player2, CardColor.GREEN);
        addCreatureReady(player1, new YavimayaBarbarian());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The Moat does not stop its controller's chosen-color creatures attacking an opponent")
    void controllerCanAttackOpponent() {
        addMoatWithChosenColor(player1, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new YavimayaBarbarian());

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        declareAttackers(player1, List.of(1));
    }

    @Test
    @DisplayName("Multiple Moats retain independent colors and flying bypasses both restrictions")
    void multipleMoatsApplyIndependentRestrictions() {
        addMoatWithChosenColor(player2, CardColor.RED);
        addMoatWithChosenColor(player2, CardColor.GREEN);
        Permanent redGreen = addCreatureReady(player1, new YavimayaBarbarian());
        Permanent green = addCreatureReady(player1, new NomadicElf());
        Permanent flyer = addCreatureReady(player1, new BlazingSpecter());

        assertThat(als.canAttackDefender(gd, redGreen, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, green, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, flyer, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Moats controlled by different players retain their own chosen colors")
    void opposingMoatsHaveIndependentColors() {
        addMoatWithChosenColor(player1, CardColor.RED);
        addMoatWithChosenColor(player2, CardColor.GREEN);
        Permanent firstElf = addCreatureReady(player1, new NomadicElf());
        Permanent secondElf = addCreatureReady(player2, new NomadicElf());

        assertThat(als.canAttackDefender(gd, firstElf, player2.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, secondElf, player1.getId())).isTrue();
    }

    private void addMoatWithChosenColor(Player controller, CardColor color) {
        Permanent moat = harness.addToBattlefieldAndReturn(controller, new TeferisMoat());
        moat.setChosenColor(color);
    }
}
