package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaggedScarArchers.class, GrizzlyBears.class, LlanowarElves.class,
        SuntailHawk.class, AngelOfMercy.class})
class JaggedScarArchersTest extends BaseCardTest {

    // ===== P/T = number of Elves you control =====

    @Test
    @DisplayName("Counts itself as an Elf when alone: 1/1")
    void countsItselfAsElf() {
        Permanent archers = addArchersReady(player1);

        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of Elves you control")
    void ptEqualsElfCount() {
        Permanent archers = addArchersReady(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());

        // itself + 2 Llanowar Elves
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only your Elves, not the opponent's")
    void countsOnlyControllersElves() {
        Permanent archers = addArchersReady(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when Elves change")
    void ptUpdatesWhenElvesChange() {
        Permanent archers = addArchersReady(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(1);
    }

    // ===== {T}: deals damage equal to its power to target creature with flying =====

    @Test
    @DisplayName("Deals damage equal to its power to a target creature with flying")
    void dealsPowerDamageToFlyer() {
        addArchersReady(player1);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.forceActivePlayer(player1);
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");

        // Archers alone is 1/1 → deals 1 to the 1/1 flyer
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Damage scales with the number of Elves you control")
    void damageScalesWithElfCount() {
        addArchersReady(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.forceActivePlayer(player1);
        UUID targetId = harness.getPermanentId(player2, "Angel of Mercy");

        // Power 3 (itself + 2 Elves) → 3 damage kills the 3/3 flyer
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        addArchersReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps the archers on activation")
    void tapsOnActivation() {
        Permanent archers = addArchersReady(player1);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.forceActivePlayer(player1);
        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(archers.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addArchersReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JaggedScarArchers());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Damage uses power at resolution rather than activation")
    void usesPowerAtResolution() {
        addArchersReady(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, angel.getId());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("Damage uses last-known power when the source leaves the battlefield")
    void usesLastKnownPowerAfterSourceLeaves() {
        Permanent archers = addArchersReady(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, angel.getId());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, archers));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jagged-Scar Archers");
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("Can target a flying creature you control")
    void canTargetOwnFlyer() {
        addArchersReady(player1);
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Suntail Hawk");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new JaggedScarArchers());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped source cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent archers = addArchersReady(player1);
        archers.tap();
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Elf creatures do not increase power or toughness")
    void doesNotCountNonElves() {
        Permanent archers = addArchersReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(1);
    }
}
