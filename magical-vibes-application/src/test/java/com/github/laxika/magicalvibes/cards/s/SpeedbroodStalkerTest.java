package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpeedbroodStalker.class, GrizzlyBears.class, HillGiant.class})
@DisplayName("Speedbrood Stalker")
class SpeedbroodStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Secretly chosen permanent is sacrificed after the opponent's chosen sacrifice")
    void secretlyChosenPermanentIsSacrificedAfterOpponentsChoice() {
        Permanent secretlyChosen = addCreature(player2, new GrizzlyBears());
        Permanent opponentChoice = addCreature(player2, new HillGiant());
        Permanent remains = addCreature(player2, new GrizzlyBears());

        castSpeedbroodStalker(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosen.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(secretlyChosen.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentChoice.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(remains.getId());
    }

    @Test
    @DisplayName("The enter ability cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SpeedbroodStalker()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, Card creature) {
        Permanent permanent = new Permanent(creature);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castSpeedbroodStalker(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SpeedbroodStalker()));
        addMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
