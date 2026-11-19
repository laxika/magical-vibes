package com.github.laxika.magicalvibes.prevention;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToTargetFromColorlessSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextColorDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageCantBePreventedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.testutil.Prevention;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Family-level regression spec for the damage-prevention machinery, pinned BEFORE collapsing the
 * shield-creating {@code Prevent*Effect} records onto a parameterized spec record. These tests fix
 * the cross-cutting <em>consumption</em> semantics of the shield state slots
 * ({@code DamagePreventionService}) that no single card test pins: consumption ordering across
 * shield kinds, non-consumption when a stronger prevention already applies, combat/noncombat
 * windows, one-decrement-per-event color prevention, and the damage-can't-be-prevented bypass.
 * Per-card creation semantics stay pinned by the ~54 user-card tests.
 */
class DamagePreventionSpecTest extends BaseCardTest {

    @Nested
    @DisplayName("Shield consumption ordering")
    class ConsumptionOrdering {

        @Test
        @DisplayName("The creature's controller orders the global and creature shields; choosing the global one spends it first")
        void globalConsumedBeforeCreatureShield() {
            Permanent bears = addCreature(player2);
            gd.addGlobalDamagePreventionShield(1);
            gd.addPermanentDamagePreventionShield(bears.getId(), 5);

            shockPermanent(bears.getId());

            // CR 616.1: two prevention effects apply to the same damage, so the affected creature's controller
            // chooses which applies first.
            PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
            assertThat(choice.playerId()).isEqualTo(player2.getId());
            harness.handleListChoice(player2, choice.options().stream()
                    .filter(option -> option.startsWith("Prevent damage with the shared shield"))
                    .findFirst().orElseThrow());

            assertThat(gd.globalDamagePreventionShield()).isZero();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(4);
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        }

        @Test
        @DisplayName("A creature shield persists across damage events until exhausted")
        void creatureShieldPersistsAcrossEvents() {
            Permanent bears = addCreature(player2);
            gd.addPermanentDamagePreventionShield(bears.getId(), 3);

            harness.setHand(player1, List.of(new Shock(), new Shock()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(1);

            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            // Second Shock: 1 prevented, 1 marked — the 2/2 survives with 1 damage.
            assertThat(gd.permanentDamagePreventionShield(bears.getId())).isZero();
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        }

        @Test
        @DisplayName("Full player prevention does not consume the player's amount shield")
        void fullPlayerPreventionDoesNotConsumeAmountShield() {
            Prevention.addOnPlayer(gd, PreventDamageEffect.allToControllerAndCreatures(), player2.getId());
            gd.addPlayerDamagePreventionShield(player2.getId(), 3);
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Combat / noncombat windows")
    class CombatWindows {

        @Test
        @DisplayName("A combat-only creature flag does not stop noncombat damage")
        void combatOnlyCreatureFlagDoesNotStopNoncombat() {
            Permanent bears = addCreature(player2);
            Prevention.addOnPermanent(gd, PreventDamageEffect.allCombatToTargetCreatures(), bears.getId());

            shockPermanent(bears.getId());

            // Shock's 2 noncombat damage kills the 2/2 despite the Foxfire-style combat flag.
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        }

        @Test
        @DisplayName("Fog's combat-only flag does not stop noncombat damage to a player")
        void fogFlagDoesNotStopNoncombatPlayerDamage() {
            Prevention.add(gd, PreventDamageEffect.allCombat());
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Prevent-all-to-creatures does not protect players")
        void allCreaturesFlagDoesNotProtectPlayers() {
            Prevention.add(gd, PreventDamageEffect.allToCreatures());
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Damage-from-attackers player flag prevents unblocked combat damage")
        void attackersFlagPreventsCombatDamage() {
            Prevention.addOnPlayer(gd, PreventDamageEffect.allToControllerFromAttackers(), player2.getId());
            harness.setLife(player2, 20);

            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @CardUsed(GrizzlyBears.class)
        @DisplayName("Take the Bait prevention protects the player and their planeswalkers")
        void controllerAndPlaneswalkerCombatPrevention() {
            Card planeswalkerCard = new Card();
            planeswalkerCard.setName("Test Planeswalker");
            planeswalkerCard.setType(CardType.PLANESWALKER);
            Permanent planeswalker = new Permanent(planeswalkerCard);
            planeswalker.setCounterCount(CounterType.LOYALTY, 5);
            gd.playerBattlefields.get(player2.getId()).add(planeswalker);

            Permanent attackerToPlayer = new Permanent(new GrizzlyBears());
            attackerToPlayer.setSummoningSick(false);
            attackerToPlayer.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attackerToPlayer);

            Permanent attackerToPlaneswalker = new Permanent(new GrizzlyBears());
            attackerToPlaneswalker.setSummoningSick(false);
            attackerToPlaneswalker.setAttacking(true);
            attackerToPlaneswalker.setAttackTarget(planeswalker.getId());
            gd.playerBattlefields.get(player1.getId()).add(attackerToPlaneswalker);

            Prevention.addOnPlayer(gd, PreventDamageEffect.allCombatToControllerAndPlaneswalkers(), player2.getId());
            harness.setLife(player2, 20);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Color prevention")
    class ColorPrevention {

        @Test
        @DisplayName("A color-prevention charge absorbs one whole damage event and is decremented once")
        void colorChargeConsumedOncePerEvent() {
            harness.setLife(player2, 20);
            gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player2.getId(),
                    new PreventNextColorDamageToControllerEffect(CardColor.RED), player2.getId(),
                    Duration.UNTIL_END_OF_TURN));

            harness.setHand(player1, List.of(new Shock(), new Shock()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            // First red event fully prevented, charge consumed.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.hasFloatingEffectOnPlayer(player2.getId(), PreventNextColorDamageToControllerEffect.class))
                    .isFalse();

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            // No charge left: the second Shock lands in full.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Target-specific colorless prevention stops colorless damage")
        void targetSpecificColorlessPreventionStopsColorlessDamage() {
            Permanent bears = addCreature(player2);
            gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, null,
                    new PreventAllDamageToTargetFromColorlessSourcesEffect(), bears.getId(),
                    Duration.UNTIL_END_OF_TURN));

            harness.setHand(player1, List.of(colorlessDamageSpell()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
            assertThat(bears.getMarkedDamage()).isZero();
        }
    }

    @Nested
    @DisplayName("Damage can't be prevented")
    class CantBePrevented {

        @Test
        @DisplayName("The turn flag bypasses shields without consuming them")
        void bypassesShieldsWithoutConsuming() {
            gd.addShield(new DamageCantBePreventedThisTurnEffect());
            gd.addGlobalDamagePreventionShield(3);
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.globalDamagePreventionShield()).isEqualTo(3);
        }
    }

    // ===== Helpers =====

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(creature);
        return creature;
    }

    private void shockPermanent(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void shockPlayer2() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private Card colorlessDamageSpell() {
        Card card = new Card();
        card.setName("Colorless Bolt");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.addSpellEffect(new DealDamageToTargetCreatureEffect(4));
        return card;
    }
}
