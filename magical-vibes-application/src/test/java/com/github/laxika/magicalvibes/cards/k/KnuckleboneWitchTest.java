package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KnuckleboneWitchTest extends BaseCardTest {

    // "Whenever a Goblin you control is put into a graveyard from the battlefield,
    //  you may put a +1/+1 counter on this creature."

    private Permanent witch() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knucklebone Witch"))
                .findFirst().orElseThrow();
    }

    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities(); // resolve Shock -> creature dies -> death trigger onto stack
        harness.passBothPriorities(); // resolve the death trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("Accepting the may ability puts a +1/+1 counter on the witch when a Goblin dies")
    void acceptingAddsCounterWhenGoblinDies() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.addToBattlefield(player1, new SkirkProspector()); // 1/1 Goblin

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        killWithShock("Skirk Prospector");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, witch())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, witch())).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability adds no counter")
    void decliningAddsNoCounter() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.addToBattlefield(player1, new SkirkProspector());

        killWithShock("Skirk Prospector");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A non-Goblin creature dying does not trigger the witch")
    void nonGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.addToBattlefield(player1, new GrizzlyBears()); // Bear, not Goblin

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        // Shock only deals 2 to a 2/2 Bear; kill it, then confirm no counter appears.
        harness.passBothPriorities();

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
