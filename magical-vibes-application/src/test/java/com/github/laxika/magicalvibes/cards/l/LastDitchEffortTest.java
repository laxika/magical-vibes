package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LastDitchEffortTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices any chosen creatures and deals that much damage to a player")
    void sacrificesChosenCreaturesAndDealsTheirCountAsDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing no creatures deals no damage")
    void sacrificingNoCreaturesDealsNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        castLastDitchEffort(player2.getId());

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Deals damage to a creature equal to the number of creatures sacrificed")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new RagingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLastDitchEffort(target.getId());

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(harness.getPermanentId(player1, "Raging Goblin")));

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void castLastDitchEffort(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LastDitchEffort()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
    }
}
