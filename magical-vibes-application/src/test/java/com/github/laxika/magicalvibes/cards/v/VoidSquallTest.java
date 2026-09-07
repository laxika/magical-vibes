package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidSquall.class, GrizzlyBears.class, Forest.class})
class VoidSquallTest extends BaseCardTest {

    @Test
    void returnsTargetNonlandPermanentToItsOwnersHandAndRebounds() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VoidSquall()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetALand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new VoidSquall()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        VoidSquall card = new VoidSquall();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Void Squall");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}
