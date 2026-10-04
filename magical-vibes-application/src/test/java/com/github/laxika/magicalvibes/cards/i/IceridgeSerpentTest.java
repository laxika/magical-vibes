package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IceridgeSerpent.class})
class IceridgeSerpentTest extends BaseCardTest {

    @Test
    void etbReturnsTargetedOpponentCreatureToItsOwnersHand() {
        harness.addToBattlefield(player2, new IceridgeSerpent());
        castSerpent(harness.getPermanentId(player2, "Iceridge Serpent"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iceridge Serpent");
        harness.assertInHand(player2, "Iceridge Serpent");
        harness.assertOnBattlefield(player1, "Iceridge Serpent");
    }

    @Test
    void cannotTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new IceridgeSerpent());
        UUID ownCreatureId = harness.getPermanentId(player1, "Iceridge Serpent");
        harness.setHand(player1, List.of(new IceridgeSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithNoOpponentCreatures() {
        harness.castFromHand(player1, new IceridgeSerpent(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Iceridge Serpent");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsOpponentControlledCreatureToItsOwnerRatherThanItsController() {
        IceridgeSerpent stolenCreature = new IceridgeSerpent();
        stolenCreature.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, stolenCreature);
        UUID targetId = harness.getPermanentId(player2, "Iceridge Serpent");
        gd.stolenCreatures.put(targetId, player1.getId());

        castSerpent(targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iceridge Serpent");
        harness.assertInHand(player1, "Iceridge Serpent");
        harness.assertNotInHand(player2, "Iceridge Serpent");
    }

    @Test
    void triggerStillReturnsCreatureAfterSerpentLeavesBattlefield() {
        harness.addToBattlefield(player2, new IceridgeSerpent());
        castSerpent(harness.getPermanentId(player2, "Iceridge Serpent"));
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToHand(
                gd, gd.playerBattlefields.get(player1.getId()).getFirst());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Iceridge Serpent");
        harness.assertInHand(player2, "Iceridge Serpent");
    }

    @Test
    void doesNotReturnTargetThatHasChangedToYourControl() {
        harness.addToBattlefield(player2, new IceridgeSerpent());
        castSerpent(harness.getPermanentId(player2, "Iceridge Serpent"));
        harness.passBothPriorities();

        var target = gd.playerBattlefields.get(player2.getId()).removeFirst();
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Iceridge Serpent");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(target);
    }

    private void castSerpent(UUID targetId) {
        harness.setHand(player1, List.of(new IceridgeSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, targetId);
    }
}
