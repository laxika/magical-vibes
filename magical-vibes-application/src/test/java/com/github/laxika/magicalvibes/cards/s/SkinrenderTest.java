package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.m.MoriokReplica;
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

@CardUsed({Skinrender.class, MoriokReplica.class, AlphaTyrranax.class})
class SkinrenderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts three -1/-1 counters on target creature")
    void etbPutsThreeCountersOnTargetCreature() {
        harness.addToBattlefield(player2, new MoriokReplica());
        UUID targetId = harness.getPermanentId(player2, "Moriok Replica");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        // Moriok Replica (2/2) with 3 -1/-1 counters â†’ -1/-1, dies to SBA
        harness.assertNotOnBattlefield(player2, "Moriok Replica");
        harness.assertInGraveyard(player2, "Moriok Replica");
    }

    @Test
    @DisplayName("ETB leaves creature alive if it has enough toughness")
    void etbLeavesLargeCreatureAlive() {
        // Alpha Tyrranax is 6/5 â€” survives 3 -1/-1 counters as 3/2
        harness.addToBattlefield(player2, new AlphaTyrranax());
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Alpha Tyrranax");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Skinrender enters battlefield even when target dies")
    void skinrenderEntersBattlefieldWhenTargetDies() {
        harness.addToBattlefield(player2, new MoriokReplica());
        UUID targetId = harness.getPermanentId(player2, "Moriok Replica");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        // Skinrender should be on the battlefield
        harness.assertOnBattlefield(player1, "Skinrender");
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new MoriokReplica());
        UUID targetId = harness.getPermanentId(player1, "Moriok Replica");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        // Own Moriok Replica dies from 3 -1/-1 counters
        harness.assertNotOnBattlefield(player1, "Moriok Replica");
    }

    @Test
    @DisplayName("Can cast without target when no creatures on battlefield")
    void canCastWithoutTargetWhenNoCreatures() {
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skinrender");
    }

    @Test
    @DisplayName("Must target itself when it is the only creature")
    void mustTargetItselfWhenItIsTheOnlyCreature() {
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Skinrender");
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Skinrender"));
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Skinrender");
        harness.assertInGraveyard(player1, "Skinrender");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new MoriokReplica());
        UUID targetId = harness.getPermanentId(player2, "Moriok Replica");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell â†’ ETB on stack

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB â†’ fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("ETB still puts counters on its target after Skinrender leaves")
    void etbResolvesAfterSkinrenderLeaves() {
        harness.addToBattlefield(player2, new AlphaTyrranax());
        UUID targetId = harness.getPermanentId(player2, "Alpha Tyrranax");
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Alpha Tyrranax");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can choose another creature after Skinrender enters without a preselected target")
    void choosesTargetAfterEntering() {
        harness.addToBattlefield(player2, new AlphaTyrranax());
        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Alpha Tyrranax"));
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Alpha Tyrranax")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Skinrender");
    }
}
