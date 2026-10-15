package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrapjawKelpie.class, Eviscerate.class})
class TrapjawKelpieTest extends BaseCardTest {

    /** Resolves the stack until the game pauses for input or the stack empties. */
    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData g = harness.getGameData();
            if (g.interaction.isAwaitingInput() || g.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    private Permanent kelpieOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Trapjaw Kelpie"))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("Persist returns Trapjaw Kelpie with a -1/-1 counter when it dies with no -1/-1 counters")
    void persistReturnsWithMinusCounter() {
        harness.addToBattlefield(player1, new TrapjawKelpie());
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Trapjaw Kelpie"));
        resolveUntilInputOrEmpty();

        Permanent kelpie = kelpieOnBattlefield();
        assertThat(kelpie).isNotNull();
        assertThat(kelpie.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(kelpie.getEffectivePower()).isEqualTo(2);
        assertThat(kelpie.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Persist does not return Trapjaw Kelpie when it died with a -1/-1 counter")
    void persistDoesNotReturnWithExistingMinusCounter() {
        Permanent kelpie = harness.addToBattlefieldAndReturn(player1, new TrapjawKelpie());
        kelpie.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0, kelpie.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Trapjaw Kelpie");
        harness.assertInGraveyard(player1, "Trapjaw Kelpie");
    }
    @Test
    @DisplayName("Flash allows casting Trapjaw Kelpie during an opponent's end step")
    void flashDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new TrapjawKelpie()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trapjaw Kelpie");
        harness.assertNotInHand(player1, "Trapjaw Kelpie");
    }

    @Test
    @DisplayName("Flash allows casting Trapjaw Kelpie in response to a spell")
    void flashInResponseToSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TrapjawKelpie());
        harness.setHand(player1, List.of(new Eviscerate(), new TrapjawKelpie()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castSorcery(player1, 0, 0, target.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Trapjaw Kelpie");
    }

    @Test
    @DisplayName("A Kelpie returned by persist stays dead after its second destruction")
    void returnedKelpieDoesNotPersistAgain() {
        harness.addToBattlefield(player1, new TrapjawKelpie());
        harness.setHand(player1, List.of(new Eviscerate(), new Eviscerate()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castSorcery(player1, 0, 0, harness.getPermanentId(player1, "Trapjaw Kelpie"));
        resolveUntilInputOrEmpty();
        harness.assertOnBattlefield(player1, "Trapjaw Kelpie");
        harness.assertNotInGraveyard(player1, "Trapjaw Kelpie");

        harness.castAndResolveSorcery(player1, 0, 0, harness.getPermanentId(player1, "Trapjaw Kelpie"));

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Trapjaw Kelpie");
        harness.assertInGraveyard(player1, "Trapjaw Kelpie");
    }
}
