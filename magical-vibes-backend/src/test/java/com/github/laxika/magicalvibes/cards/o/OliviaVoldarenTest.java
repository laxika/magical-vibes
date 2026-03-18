package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetPermanentWhileSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OliviaVoldarenTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("First ability has correct effects: damage, grant subtype, +1/+1 counter")
    void firstAbilityHasCorrectEffects() {
        OliviaVoldaren card = new OliviaVoldaren();

        assertThat(card.getActivatedAbilities()).hasSize(2);
        var firstAbility = card.getActivatedAbilities().get(0);
        assertThat(firstAbility.getManaCost()).isEqualTo("{1}{R}");
        assertThat(firstAbility.isRequiresTap()).isFalse();
        assertThat(firstAbility.getEffects())
                .hasSize(3)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(DealDamageToTargetCreatureEffect.class);
                    assertThat(effects.get(1)).isInstanceOf(GrantSubtypeToTargetCreatureEffect.class);
                    assertThat(effects.get(2)).isInstanceOf(PutCounterOnSelfEffect.class);
                });
    }

    @Test
    @DisplayName("Second ability has correct effects: gain control while source")
    void secondAbilityHasCorrectEffects() {
        OliviaVoldaren card = new OliviaVoldaren();

        var secondAbility = card.getActivatedAbilities().get(1);
        assertThat(secondAbility.getManaCost()).isEqualTo("{3}{B}{B}");
        assertThat(secondAbility.isRequiresTap()).isFalse();
        assertThat(secondAbility.getEffects())
                .hasSize(1)
                .first().isInstanceOf(GainControlOfTargetPermanentWhileSourceEffect.class);
    }

    // ===== First ability: {1}{R} ping + Vampire + counter =====

    @Nested
    @DisplayName("First ability: {1}{R} ping")
    class FirstAbility {

        @Test
        @DisplayName("Deals 1 damage to target creature, makes it a Vampire, puts +1/+1 counter on Olivia")
        void fullFirstAbilityResolution() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            Permanent target = addReadyCreature(player2);

            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);

            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, null, target.getId());
            harness.passBothPriorities();

            // Target creature took 1 damage (Grizzly Bears 2/2 -> 1 damage marked)
            assertThat(target.getMarkedDamage()).isEqualTo(1);

            // Target creature is now a Vampire
            assertThat(target.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);

            // Olivia gets a +1/+1 counter (3/3 base -> effectively 4/4)
            assertThat(olivia.getPlusOnePlusOneCounters()).isEqualTo(1);
            assertThat(gqs.getEffectivePower(gd, olivia)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, olivia)).isEqualTo(4);
        }

        @Test
        @DisplayName("Cannot target Olivia herself (must be 'another' creature)")
        void cannotTargetSelf() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);

            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);

            assertThatThrownBy(() -> harness.activateAbility(player1, oliviaIdx, null, olivia.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be another creature");
        }

        @Test
        @DisplayName("Can activate multiple times to accumulate counters")
        void multipleActivations() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            // Add two creatures as targets
            Permanent target1 = addReadyCreature(player2);
            Permanent target2 = addReadyCreature(player2);

            // First activation
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, null, target1.getId());
            harness.passBothPriorities();

            // Second activation
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            harness.activateAbility(player1, oliviaIdx, null, target2.getId());
            harness.passBothPriorities();

            assertThat(olivia.getPlusOnePlusOneCounters()).isEqualTo(2);
            assertThat(gqs.getEffectivePower(gd, olivia)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, olivia)).isEqualTo(5);
        }

        @Test
        @DisplayName("Kills a 1-toughness creature with the damage")
        void killsOneToughnessCreature() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            // Llanowar Elves is 1/1
            harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
            Permanent elves = findPermanent(player2, "Llanowar Elves");

            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);

            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, null, elves.getId());
            harness.passBothPriorities();

            // Elves should be dead (state-based actions destroy it)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));

            // Olivia still gets the +1/+1 counter
            assertThat(olivia.getPlusOnePlusOneCounters()).isEqualTo(1);
        }
    }

    // ===== Second ability: {3}{B}{B} gain control of Vampire =====

    @Nested
    @DisplayName("Second ability: {3}{B}{B} steal Vampire")
    class SecondAbility {

        @Test
        @DisplayName("Gains control of target Vampire")
        void gainsControlOfVampire() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            // Add a Vampire to opponent's battlefield
            harness.addToBattlefield(player2, new BaronyVampire());
            Permanent barony = findPermanent(player2, "Barony Vampire");
            barony.setSummoningSick(false);

            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.BLACK, 2);

            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, 1, null, barony.getId());
            harness.passBothPriorities();

            // Barony Vampire is now controlled by player1
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(barony.getId()));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getId().equals(barony.getId()));

            // Tracked as source-dependent steal
            assertThat(gd.sourceDependentStolenCreatures).containsKey(barony.getId());
            assertThat(gd.sourceDependentStolenCreatures.get(barony.getId())).isEqualTo(olivia.getId());
        }

        @Test
        @DisplayName("Cannot target a non-Vampire creature")
        void cannotTargetNonVampire() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            Permanent bears = addReadyCreature(player2);

            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.BLACK, 2);

            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);

            assertThatThrownBy(() -> harness.activateAbility(player1, oliviaIdx, 1, null, bears.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Target must be a Vampire creature");
        }

        @Test
        @DisplayName("Can target a creature that was made a Vampire by Olivia's first ability")
        void canTargetCreatureMadeVampireByFirstAbility() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            Permanent bears = addReadyCreature(player2);

            // First: ping to make it a Vampire
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.RED, 1);
            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, null, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getGrantedSubtypes()).contains(CardSubtype.VAMPIRE);

            // Second: steal it
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.BLACK, 2);
            harness.activateAbility(player1, oliviaIdx, 1, null, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(bears.getId()));
        }

        @Test
        @DisplayName("Stolen creature returns when Olivia is bounced")
        void stolenCreatureReturnsWhenOliviaBounced() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            harness.addToBattlefield(player2, new BaronyVampire());
            Permanent barony = findPermanent(player2, "Barony Vampire");
            barony.setSummoningSick(false);

            // Steal the Vampire
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.BLACK, 2);
            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, 1, null, barony.getId());
            harness.passBothPriorities();

            // Confirm steal worked
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getId().equals(barony.getId()));

            // Bounce Olivia with Unsummon
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new Unsummon()));
            harness.addMana(player2, ManaColor.BLUE, 1);

            harness.passPriority(player1);
            harness.castInstant(player2, 0, olivia.getId());
            harness.passBothPriorities();

            // Barony Vampire should return to player2
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(barony.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(barony.getId()));

            // Tracking should be cleaned up
            assertThat(gd.stolenCreatures).doesNotContainKey(barony.getId());
            assertThat(gd.sourceDependentStolenCreatures).doesNotContainKey(barony.getId());
        }

        @Test
        @DisplayName("Ability resolves with no effect if Olivia leaves before resolution (ruling)")
        void abilityNoEffectIfOliviaLeavesBeforeResolution() {
            harness.addToBattlefield(player1, new OliviaVoldaren());
            Permanent olivia = findPermanent(player1, "Olivia Voldaren");
            olivia.setSummoningSick(false);

            harness.addToBattlefield(player2, new BaronyVampire());
            Permanent barony = findPermanent(player2, "Barony Vampire");
            barony.setSummoningSick(false);

            // Activate steal ability
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.BLACK, 2);
            int oliviaIdx = gd.playerBattlefields.get(player1.getId()).indexOf(olivia);
            harness.activateAbility(player1, oliviaIdx, 1, null, barony.getId());

            // Remove Olivia before the ability resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getId().equals(olivia.getId()));

            harness.passBothPriorities();

            // Barony Vampire should NOT have been stolen
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getId().equals(barony.getId()));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getId().equals(barony.getId()));

            // Log should mention no effect
            assertThat(gd.gameLog).anyMatch(log -> log.contains("no effect"));
        }
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
