package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Incriminate.class, GrizzlyBears.class, HillGiant.class})
class IncriminateTest extends BaseCardTest {

    private void castIncriminate(Permanent first, Permanent second) {
        harness.setHand(player1, List.of(new Incriminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The shared controller chooses which targeted creature to sacrifice")
    void sharedControllerChoosesCreatureToSacrifice() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castIncriminate(bears, giant);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());
    }

    @Test
    @DisplayName("Sacrifices the creature chosen by the shared controller")
    void sacrificesChosenCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        castIncriminate(bears, giant);
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(giant);
    }

    @Test
    @DisplayName("Sacrifices the sole remaining legal target")
    void sacrificesSoleRemainingTarget() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new Incriminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, List.of(bears.getId(), giant.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Requires both targets to be controlled by the same player")
    void cannotTargetCreaturesControlledByDifferentPlayers() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent theirs = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new Incriminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(own.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
