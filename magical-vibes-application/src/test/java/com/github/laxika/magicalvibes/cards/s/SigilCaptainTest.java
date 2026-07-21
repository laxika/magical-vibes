package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SigilCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on a 1/1 creature that enters (mandatory, no prompt)")
    void putsTwoCountersOn1_1() {
        harness.addToBattlefield(player1, new SigilCaptain());

        harness.setHand(player1, List.of(new FugitiveWizard())); // 1/1
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell -> it enters, Sigil Captain triggers
        harness.passBothPriorities(); // resolve Sigil Captain's mandatory trigger

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wizard = findByName(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger for a creature that is not 1/1")
    void noTriggerForNon1_1() {
        harness.addToBattlefield(player1, new SigilCaptain());

        harness.setHand(player1, List.of(new GrizzlyBears())); // 2/2
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findByName(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's 1/1 creature")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new SigilCaptain());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve opponent's creature spell

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(findByName(player2, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent findByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
