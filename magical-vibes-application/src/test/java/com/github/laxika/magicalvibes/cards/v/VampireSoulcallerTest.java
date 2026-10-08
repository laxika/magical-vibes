package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireSoulcaller.class, GrizzlyBears.class, Forest.class, BearCub.class})
class VampireSoulcallerTest extends BaseCardTest {

    private void castVampireSoulcaller() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new VampireSoulcaller(), "{4}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card from the graveyard to hand")
    void etbReturnsCreatureToHand() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castVampireSoulcaller();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature card is not a legal graveyard target")
    void noncreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new Forest()));

        castVampireSoulcaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new VampireSoulcaller());
        var attacker = addCreatureReady(player1, new BearCub());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void opponentsGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new BearCub()));

        castVampireSoulcaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Vampire Soulcaller");
        harness.assertInGraveyard(player2, "Bear Cub");
        harness.assertNotInHand(player1, "Bear Cub");
    }

    @Test
    void missingTargetDoesNotReturnAnotherCreature() {
        BearCub target = new BearCub();
        BearCub other = new BearCub();
        harness.setGraveyard(player1, List.of(target, other));

        castVampireSoulcaller();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Bear Cub");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        harness.assertOnBattlefield(player1, "Vampire Soulcaller");
    }

    @Test
    void emptyGraveyardDoesNotPreventEntering() {
        harness.setGraveyard(player1, List.of());

        castVampireSoulcaller();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Vampire Soulcaller");
    }
}
