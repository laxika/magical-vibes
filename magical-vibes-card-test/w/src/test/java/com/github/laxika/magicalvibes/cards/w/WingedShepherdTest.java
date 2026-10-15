package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingedShepherd.class, GrizzlyBears.class})
class WingedShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new WingedShepherd()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Winged Shepherd");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling pays the discard cost before the draw resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new WingedShepherd()));
        harness.setLibrary(player1, List.of(new WingedShepherd(), new WingedShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Winged Shepherd");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Winged Shepherd");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with blue mana")
    void cyclingRequiresWhiteMana() {
        harness.setHand(player1, List.of(new WingedShepherd()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Winged Shepherd");
        harness.assertNotInGraveyard(player1, "Winged Shepherd");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance allows attacking without tapping")
    void attackingDoesNotTapShepherd() {
        Permanent shepherd = addCreatureReady(player1, new WingedShepherd());

        declareAttackers(List.of(0));

        assertThat(shepherd.isAttacking()).isTrue();
        assertThat(shepherd.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new WingedShepherd());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Another flying creature can block Winged Shepherd")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new WingedShepherd());
        Permanent blocker = addCreatureReady(player2, new WingedShepherd());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
