package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WickerboughElderTest extends BaseCardTest {

    // ===== ETB: enters with a -1/-1 counter =====

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter (4/4 becomes 3/3)")
    void entersWithMinusCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WickerboughElder()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent elder = findElder(player1);
        assertThat(elder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elder.getEffectivePower()).isEqualTo(3);
        assertThat(elder.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Ability removes a -1/-1 counter and destroys target artifact")
    void abilityDestroysArtifact() {
        Permanent elder = addReadyElder(player1);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(elder.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angel's Feather"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angel's Feather"));
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void abilityDestroysEnchantment() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    // ===== Cannot activate without a counter =====

    @Test
    @DisplayName("Cannot activate ability when no -1/-1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent elder = addReadyElder(player1);
        elder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.addToBattlefield(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    // ===== Illegal target =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyElder(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    // ===== Helpers =====

    private Permanent addReadyElder(Player player) {
        WickerboughElder card = new WickerboughElder();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findElder(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wickerbough Elder"))
                .findFirst().orElseThrow();
    }
}
