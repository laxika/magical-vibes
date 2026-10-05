package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.Wispmare;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnuckleboneWitch.class, Shock.class, SkirkProspector.class, GrizzlyBears.class,
        BoggartShenanigans.class, Wispmare.class, Tarfire.class})
class KnuckleboneWitchTest extends BaseCardTest {

    // "Whenever a Goblin you control is put into a graveyard from the battlefield,
    //  you may put a +1/+1 counter on this creature."

    private Permanent witch() {
        return findPermanent(player1, "Knucklebone Witch");
    }

    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
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
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Goblin dying does not trigger the witch")
    void opponentGoblinDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.addToBattlefield(player2, new SkirkProspector());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Skirk Prospector"));

        harness.assertInGraveyard(player2, "Skirk Prospector");
        assertThat(gd.stack).isEmpty();
        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A Goblin noncreature permanent going to the graveyard triggers the witch")
    void goblinEnchantmentGoingToGraveyardAddsCounter() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.addToBattlefield(player1, new BoggartShenanigans());
        harness.setHand(player1, List.of(new Wispmare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0,
                harness.getPermanentId(player1, "Boggart Shenanigans"));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Goblin spell going to the graveyard from the stack does not trigger the witch")
    void goblinInstantResolvingDoesNotTrigger() {
        harness.addToBattlefield(player1, new KnuckleboneWitch());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Tarfire");
        assertThat(gd.stack).isEmpty();
        assertThat(witch().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
