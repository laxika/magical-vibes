package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SilvercladFerocidonsTest extends BaseCardTest {

    @Test
    void damageTriggersOpponentToSacrificePermanent() {
        harness.addToBattlefield(player2, new SilvercladFerocidons());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherOpponentPermanent = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ferocidonsId = harness.getPermanentId(player2, "Silverclad Ferocidons");
        harness.castInstant(player1, 0, ferocidonsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(opponentPermanent.getId(), otherOpponentPermanent.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(opponentPermanent.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Silverclad Ferocidons");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
