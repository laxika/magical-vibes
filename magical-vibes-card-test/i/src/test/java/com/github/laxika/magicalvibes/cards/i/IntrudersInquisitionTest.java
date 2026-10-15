package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IntrudersInquisition.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class IntrudersInquisitionTest extends BaseCardTest {

    @Test
    void dealsPowerDamageAndMakesTheCreatureControllerDiscardGreatestManaValueCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        cast(source, target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotDiscardWhenNoExcessDamageWasDealt() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest()));
        cast(source, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void requiresAControlledCreatureAndAnOpponentCreature() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentSource = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new IntrudersInquisition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(opponentSource.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent source, Permanent target) {
        harness.setHand(player1, List.of(new IntrudersInquisition()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
