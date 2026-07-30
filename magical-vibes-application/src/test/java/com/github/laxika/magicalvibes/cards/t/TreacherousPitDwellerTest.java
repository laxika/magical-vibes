package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreacherousPitDwellerTest extends BaseCardTest {

    private static final String NAME = "Treacherous Pit-Dweller";

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gd = harness.getGameData();
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent pitDweller(GameData gd, com.github.laxika.magicalvibes.model.Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(NAME))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Casting it from hand does not trigger the control-change ability")
    void castFromHandDoesNotTrigger() {
        harness.setHand(player1, List.of(new TreacherousPitDweller()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(pitDweller(gd, player1)).isNotNull();
        assertThat(pitDweller(gd, player2)).isNull();
    }

    @Test
    @DisplayName("Undying return from the graveyard gives the opponent control of it")
    void undyingReturnGivesOpponentControl() {
        harness.addToBattlefield(player1, new TreacherousPitDweller());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, NAME));
        resolveUntilInputOrEmpty();

        GameData gd = harness.getGameData();
        // Bolt killed it (4/3); undying returned it with a +1/+1 counter and the
        // enters-from-graveyard trigger is asking for the target opponent.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        Permanent returned = pitDweller(gd, player1);
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(pitDweller(gd, player1)).isNull();
        assertThat(pitDweller(gd, player2)).isNotNull();
    }

    @Test
    @DisplayName("Undying does not return it when it died with a +1/+1 counter, so no control change")
    void diesWithCounterNoReturnAndNoTrigger() {
        Permanent dweller = harness.addToBattlefieldAndReturn(player1, new TreacherousPitDweller());
        dweller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 5/4
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, dweller.getId());
        resolveUntilInputOrEmpty();
        harness.castInstant(player1, 0, dweller.getId());
        resolveUntilInputOrEmpty();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, NAME);
        assertThat(pitDweller(gd, player1)).isNull();
        assertThat(pitDweller(gd, player2)).isNull();
    }
}
