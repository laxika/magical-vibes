package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NeedletoothRaptorTest extends BaseCardTest {

    @Test
    void damageTriggersFiveDamageToAnOpponentsCreature() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID raptorId = harness.getPermanentId(player2, "Needletooth Raptor");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bearsId);
        harness.handlePermanentChosen(player2, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Needletooth Raptor");
    }

    @Test
    void damageTriggerCannotTargetYourOwnCreature() {
        harness.addToBattlefield(player2, new NeedletoothRaptor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID raptorId = harness.getPermanentId(player2, "Needletooth Raptor");
        harness.castInstant(player1, 0, raptorId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Needletooth Raptor");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
