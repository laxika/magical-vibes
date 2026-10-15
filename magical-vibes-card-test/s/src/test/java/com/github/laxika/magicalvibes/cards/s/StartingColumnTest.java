package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StartingColumn.class, Forest.class, GrizzlyBears.class})
class StartingColumnTest extends BaseCardTest {

    @Test
    void startsEnginesWhenItEntersTheBattlefield() {
        harness.addToBattlefield(player1, new StartingColumn());

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void tapsForManaOfAnyColor() {
        harness.addToBattlefield(player1, new StartingColumn());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        Permanent column = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(column.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void atMaxSpeedSacrificesAndDrawsTwoThenDiscards() {
        Permanent column = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        Forest discarded = new Forest();
        Forest drawnOne = new Forest();
        GrizzlyBears drawnTwo = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(column).isNotIn(gd.playerBattlefields.get(player1.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
    }

    @Test
    void maxSpeedAbilityCannotBeActivatedBelowMaxSpeed() {
        Permanent column = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        gd.playerSpeeds.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(column).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void canDiscardANewlyDrawnCardWithAnInitiallyEmptyHand() {
        StartingColumn card = new StartingColumn();
        harness.addToBattlefield(player1, card);
        Forest drawnOne = new Forest();
        Forest drawnTwo = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne, drawnTwo);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnOne);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card, drawnTwo);
    }

    @Test
    void tappedColumnCannotActivateEitherAbility() {
        Permanent column = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        column.tap();
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(column).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsMaxSpeedDoesNotEnableControllersAbility() {
        Permanent column = harness.addToBattlefieldAndReturn(player1, new StartingColumn());
        gd.playerSpeeds.put(player1.getId(), 1);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");

        assertThat(column.isTapped()).isFalse();
        assertThat(column).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringAtAnExistingSpeedDoesNotResetIt() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.addToBattlefield(player1, new StartingColumn());

        harness.runStateBasedActions();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }
}
