package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.n.NyleasForerunner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodsPunishment.class, NyxbornCourser.class, NyleasForerunner.class})
class HeliodsPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod's Punishment enters with four task counters")
    void entersWithFourTaskCounters() {
        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        harness.setHand(player1, List.of(new HeliodsPunishment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent punishment = findPermanent(player1, "Heliod's Punishment");
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature cannot attack or block")
    void enchantedCreatureCannotAttackOrBlock() {
        Permanent enchanted = addCreatureReady(player1, new NyxbornCourser());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        Permanent attacker = addCreatureReady(player2, new NyxbornCourser());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("The enchanted creature removes a task counter from the Aura and destroys it at zero")
    void grantedAbilityUsesAuraCountersAndDestroysAuraAtZero() {
        Permanent enchanted = addCreatureReady(player1, new NyxbornCourser());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());
        punishment.setCounterCount(CounterType.TASK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(punishment.getCounterCount(CounterType.TASK)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(punishment);
        harness.assertInGraveyard(player2, "Heliod's Punishment");
    }

    @Test
    @DisplayName("The granted ability can destroy an Aura that already has zero task counters")
    void grantedAbilityCanBeActivatedWithoutTaskCounters() {
        Permanent enchanted = addCreatureReady(player1, new NyxbornCourser());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(enchanted.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Heliod's Punishment");
    }

    @Test
    @DisplayName("Resolving the granted ability removes only one task counter and leaves the Aura at three")
    void removesOneCounterOnResolution() {
        Permanent enchanted = addCreatureReady(player1, new NyxbornCourser());
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());
        punishment.setCounterCount(CounterType.TASK, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Heliod's Punishment");
    }

    @Test
    @DisplayName("The Aura removes intrinsic and static abilities, which return when it is destroyed")
    void abilitiesReturnAfterPunishmentIsDestroyed() {
        Permanent enchanted = addCreatureReady(player1, new NyleasForerunner());
        Permanent other = addCreatureReady(player1, new NyxbornCourser());
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());
        punishment.setCounterCount(CounterType.TASK, 1);

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Heliod's Punishment");
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("With two Punishments, only the most recently attached Aura loses a counter")
    void onlyMostRecentPunishmentGrantsAnAbility() {
        Permanent enchanted = addCreatureReady(player1, new NyxbornCourser());
        harness.setHand(player2, List.of(new HeliodsPunishment(), new HeliodsPunishment()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, enchanted.getId());
        harness.passBothPriorities();
        Permanent older = findPermanent(player2, "Heliod's Punishment");
        harness.castEnchantment(player2, 0, enchanted.getId());
        harness.passBothPriorities();
        Permanent newer = findPermanents(player2, "Heliod's Punishment").get(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(older.getCounterCount(CounterType.TASK)).isEqualTo(4);
        assertThat(newer.getCounterCount(CounterType.TASK)).isEqualTo(3);
    }

    @Test
    @DisplayName("A summoning-sick creature cannot pay the granted tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        enchanted.setSummoningSick(true);
        Permanent punishment = harness.addToBattlefieldAndReturn(player2, new HeliodsPunishment());
        punishment.setAttachedTo(enchanted.getId());
        punishment.setCounterCount(CounterType.TASK, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchanted.isTapped()).isFalse();
        assertThat(punishment.getCounterCount(CounterType.TASK)).isEqualTo(4);
    }
}
