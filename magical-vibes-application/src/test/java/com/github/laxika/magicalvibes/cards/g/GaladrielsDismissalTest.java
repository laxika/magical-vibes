package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaladrielsDismissal.class, GrizzlyBears.class, Island.class})
class GaladrielsDismissalTest extends BaseCardTest {

    @Test
    void phasesOutTargetCreatureWithoutKicker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        castAndResolve(1, creature.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
    }

    @Test
    void phasesOutAllCreaturesControlledByTargetPlayerWhenKicked() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());

        castAndResolve(4, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
    }

    @Test
    void kickerChangesTheTargetToAPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new GaladrielsDismissal()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");

        harness.setHand(player1, java.util.List.of(new GaladrielsDismissal()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only target players");
    }

    private void castAndResolve(int mana, UUID targetId) {
        harness.setHand(player1, java.util.List.of(new GaladrielsDismissal()));
        harness.addMana(player1, ManaColor.WHITE, mana);
        if (mana == 4) {
            harness.castKickedInstant(player1, 0, targetId);
            harness.passBothPriorities();
        } else {
            harness.castAndResolveInstant(player1, 0, targetId);
        }
    }
}
