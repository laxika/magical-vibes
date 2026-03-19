package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NeurokInvisimancerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Neurok Invisimancer has correct effects")
    void hasCorrectEffects() {
        NeurokInvisimancer card = new NeurokInvisimancer();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantBeBlockedEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MakeCreatureUnblockableEffect.class);
    }

    // ===== ETB makes target unblockable =====

    @Test
    @DisplayName("ETB makes target creature unblockable this turn")
    void etbMakesTargetCreatureUnblockable() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Neurok Invisimancer"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent target = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    // ===== Can target own creature =====

    @Test
    @DisplayName("Can target own creature with ETB")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no creatures on battlefield")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Neurok Invisimancer"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NeurokInvisimancer()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
