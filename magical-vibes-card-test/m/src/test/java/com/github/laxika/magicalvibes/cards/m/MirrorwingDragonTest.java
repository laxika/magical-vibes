package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvacynsJudgment;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorwingDragon.class, GrizzlyBears.class, Shock.class,
        AvacynsJudgment.class, EntrancingMelody.class})
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

    @Test
    @DisplayName("Shock copy resolves on the other creature before the original spell")
    void copyResolvesBeforeOriginal() {
        UUID dragonId = putDragonAndBearsOnBattlefield();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dragonId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Mirrorwing Dragon").getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Mirrorwing Dragon").getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Divided damage is redirected to the creature targeted by the copy")
    void dividedDamageCopyHitsOtherCreature() {
        UUID dragonId = putDragonAndBearsOnBattlefield();
        harness.setHand(player1, List.of(new AvacynsJudgment()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, Map.of(dragonId, 2));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Mirrorwing Dragon").getMarkedDamage()).isZero();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Mirrorwing Dragon").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copy eligibility uses the value of X chosen for the original spell")
    void copyEligibilityUsesChosenX() {
        UUID dragonId = putDragonOnBattlefield();
        UUID otherDragonId = harness.addToBattlefieldAndReturn(player1, new MirrorwingDragon()).getId();
        harness.setHand(player1, List.of(new EntrancingMelody()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castSorcery(player1, 0, 5, dragonId);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(otherDragonId);
    }

    private UUID putDragonOnBattlefield() {
        return harness.addToBattlefieldAndReturn(player1, new MirrorwingDragon()).getId();
    }

    private UUID putDragonAndBearsOnBattlefield() {
        UUID dragonId = putDragonOnBattlefield();
        harness.addToBattlefield(player1, new GrizzlyBears());
        return dragonId;
    }
}
