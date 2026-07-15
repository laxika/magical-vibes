package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FireImpTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fire Imp puts ETB triggered ability on the stack with the chosen target")
    void resolvingCreaturePutsEtbOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireImp()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature spell → ETB triggers

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fire Imp"));
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Fire Imp");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB deals 2 damage to target creature and it survives if tough enough")
    void etbDeals2DamageToCreature() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setPower(3);
        bear.setToughness(3);
        harness.addToBattlefield(player2, bear);
        harness.setHand(player1, List.of(new FireImp()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB

        assertThat(gd.stack).isEmpty();
        Permanent survivor = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(survivor.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB kills a 2-toughness target creature")
    void etbKills2Toughness() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
        harness.setHand(player1, List.of(new FireImp()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireImp()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature — ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // Resolve ETB — fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
