package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.g.GrendelSpawnOfKnull;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomEddieBrock.class, FlameSlash.class, GrendelSpawnOfKnull.class,
        GrizzlyBears.class})
class VenomEddieBrockTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature dying puts a +1/+1 counter on Venom")
    void getsCounterWhenAnotherCreatureDies() {
        Permanent venom = harness.addToBattlefieldAndReturn(player1, new VenomEddieBrock());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSlash(), new GrizzlyBears()));
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();

        killWithFlameSlash(player1, player2, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast - 1);
    }

    @Test
    @DisplayName("A dying Villain also makes Venom's controller draw a card")
    void drawsWhenVillainDies() {
        Permanent venom = harness.addToBattlefieldAndReturn(player1, new VenomEddieBrock());
        harness.addToBattlefield(player2, new GrendelSpawnOfKnull());
        harness.setHand(player1, List.of(new FlameSlash(), new GrizzlyBears()));
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();

        killWithFlameSlash(player1, player2, "Grendel, Spawn of Knull");
        resolveAllTriggers();

        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast);
    }

    @Test
    @DisplayName("Venom's own death does not trigger its ability")
    void ownDeathDoesNotTrigger() {
        Permanent venom = harness.addToBattlefieldAndReturn(player1, new VenomEddieBrock());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, venom.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void killWithFlameSlash(Player caster, Player targetController, String targetName) {
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
