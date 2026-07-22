package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorwingDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting instant targeting only Mirrorwing triggers its ability")
    void spellTargetingMirrorwingTriggers() {
        UUID dragonId = putDragonAndBearsOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragonId);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Mirrorwing Dragon");
    }

    @Test
    @DisplayName("Triggered ability creates a copy targeting each other creature the caster controls")
    void createsCopiesForOtherControlledCreatures() {
        UUID dragonId = putDragonAndBearsOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, dragonId);
        harness.passBothPriorities(); // resolve trigger → create copy

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("Opponent targeting your Mirrorwing copies for opponent's creatures, not yours")
    void opponentSpellCopiesForOpponentCreatures() {
        UUID dragonId = putDragonOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castInstant(player2, 0, dragonId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(opponentBearsId);
    }

    @Test
    @DisplayName("No trigger when spell targets a different creature")
    void noTriggerWhenTargetingOtherCreature() {
        putDragonAndBearsOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("No trigger when spell targets a player")
    void noTriggerWhenTargetingPlayer() {
        putDragonOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("No copies when caster controls no other legal creature targets")
    void noCopiesWithoutOtherCreatures() {
        UUID dragonId = putDragonOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragonId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        assertThat(gd.stack.getFirst().isCopy()).isFalse();
    }

    private UUID putDragonOnBattlefield() {
        harness.addToBattlefield(player1, new MirrorwingDragon());
        return harness.getPermanentId(player1, "Mirrorwing Dragon");
    }

    private UUID putDragonAndBearsOnBattlefield() {
        UUID dragonId = putDragonOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        return dragonId;
    }
}
