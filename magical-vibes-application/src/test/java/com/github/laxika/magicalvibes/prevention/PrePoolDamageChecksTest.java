package com.github.laxika.magicalvibes.prevention;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.Prevention;
import com.github.laxika.magicalvibes.testutil.TestStackDoor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage handlers outside the damage records leave every prevention to the damaged object's or player's damage pool:
 * no handler asks first whether its source is prevented from dealing damage, whether a source or spell prevention
 * applies, or whether the recipient has protection, so a second prevention is ordered by the recipient's controller
 * (CR 616.1; protection is a prevention, CR 615.1, 702.16e), and damage whose source says it can't be prevented isn't
 * (CR 615.12).
 */
@CardUsed({GrizzlyBears.class, Malignus.class, Pacifism.class, PreyUpon.class, ProdigalPyromancer.class, Shock.class,
        SilverKnight.class})
class PrePoolDamageChecksTest extends BaseCardTest {

    private static final String PERMANENT_SHIELD = "Prevent the next damage to it";

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Nested
    @DisplayName("A prevention of damage from red sources beside a shield: the damaged object's controller orders them")
    class SourcePrevention {

        @BeforeEach
        void preventRed() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
        }

        @Test
        @DisplayName("Damage to target player and each creature they control")
        void targetAndTheirCreatures() {
            Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));
            redAbility(player2.getId(), new DealDamageToTargetAndTheirCreaturesEffect(new Fixed(2)));
            chooseShieldFirst(bears, 3);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Damage divided evenly among the creatures target player controls")
        void dividedEvenly() {
            Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));
            redAbility(player2.getId(), new DealDamageDividedEvenlyAmongCreaturesTargetControlsEffect(new Fixed(2)));
            chooseShieldFirst(bears, 3);
        }

        @Test
        @DisplayName("Damage to each player and each creature they control")
        void eachPlayerAndTheirCreatures() {
            Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));
            redAbility(null, new DealDamageToEachPlayerAndTheirCreaturesEqualToControlledCreatureCountEffect());
            chooseShieldFirst(bears, 4);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Damage to the creature the trigger event was about")
        void triggeringCreature() {
            Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));
            StackEntry entry = TestStackDoor.triggered(new Shock(), player1.getId(), "Damage",
                    List.of(DealDamageEffect.to(Subjects.triggering(), 2)));
            entry.setTriggeringPermanentId(bears.getId());
            gd.stack.add(entry);
            harness.passBothPriorities();
            chooseShieldFirst(bears, 3);
        }

        @Test
        @DisplayName("Damage to the player whose end step it is: the player orders it with their shield")
        void endStepPlayer() {
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);
            redAbility(player2.getId(), new DealDamageToEndStepPlayerUnlessOpponentWasDealtDamageEffect(2));
            choosePlayerShieldFirst(player2);
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
        }
    }

    @Nested
    @DisplayName("A creature prevented from dealing damage")
    class PreventedDealer {

        @Test
        @DisplayName("Its damage to itself and a shield on it: its controller orders them")
        void eachCreatureToItself() {
            Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));
            Prevention.addOnPermanent(gd, PreventDamageEffect.allByTargetCreatures(), bears.getId());
            redAbility(null, new EachCreatureDealsPowerDamageToItselfEffect(null));
            chooseShieldFirst(bears, 3);
        }
    }

    @Nested
    @DisplayName("Protection from a creature that deals the damage itself")
    class ProtectionFromDealer {

        @Test
        @DisplayName("A red creature fighting a creature with protection from red and a shield: its controller orders them")
        void fight() {
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Permanent knight = shielded(addCreatureReady(player2, new SilverKnight()));
            harness.setHand(player1, List.of(new PreyUpon()));
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.castSorcery(player1, 0, List.of(pyromancer.getId(), knight.getId()));
            harness.passBothPriorities();

            chooseShieldFirst(knight, 4);
        }

        @Test
        @DisplayName("Prevents an enchanted red creature's damage to a creature with protection from red")
        void enchantedCreaturePrevented() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            enchantedPyromancerDeals(knight);
            assertThat(knight.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("An enchanted red creature's damage and a shield on the protected creature: its controller orders them")
        void enchantedCreatureOrdered() {
            Permanent knight = shielded(addCreatureReady(player2, new SilverKnight()));
            enchantedPyromancerDeals(knight);
            chooseShieldFirst(knight, 3);
        }

        /** A white Aura's ability makes the red creature it enchants deal 2 damage to {@code target}. */
        private void enchantedPyromancerDeals(Permanent target) {
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
            aura.setAttachedTo(pyromancer.getId());
            resolve(aura.getCard(), target.getId(), aura.getId(),
                    new EnchantedCreatureDealsDamageToTargetCreatureEffect(2, null));
        }
    }

    @Test
    @DisplayName("Damage to the creature the trigger event was about is logged once")
    void triggeringCreatureDamageLoggedOnce() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        StackEntry entry = TestStackDoor.triggered(new Shock(), player1.getId(), "Damage",
                List.of(DealDamageEffect.to(Subjects.triggering(), 1)));
        entry.setTriggeringPermanentId(bears.getId());
        gd.stack.add(entry);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(line -> line.contains("deals 1 damage to Grizzly Bears"))).hasSize(1);
    }

    @Test
    @DisplayName("An ability's damage to a creature can't be prevented when its source's damage can't be")
    void sourceDamageCantBePreventedToACreature() {
        Permanent malignus = addCreatureReady(player1, new Malignus());
        Permanent bears = shielded(addCreatureReady(player2, new GrizzlyBears()));

        resolve(malignus.getCard(), bears.getId(), malignus.getId(), DealDamageEffect.toTargetCreature(1));

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.permanentDamagePreventionShield(bears.getId())).isEqualTo(5);
    }

    private void redAbility(UUID targetId, CardEffect effect) {
        resolve(new Shock(), targetId, null, effect);
    }

    private void resolve(Card card, UUID targetId, UUID sourcePermanentId, CardEffect effect) {
        gd.stack.add(TestStackDoor.triggered(card, player1.getId(), "Damage", List.of(effect), targetId,
                sourcePermanentId));
        harness.passBothPriorities();
    }

    private Permanent shielded(Permanent permanent) {
        gd.addPermanentDamagePreventionShield(permanent.getId(), 5);
        return permanent;
    }

    private PendingInteraction.ColorChoice orderPrompt() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        return choice != null && choice.context() instanceof ChoiceContext.ReplacementEffectOrder ? choice : null;
    }

    private void chooseShieldFirst(Permanent creature, int shieldLeft) {
        PendingInteraction.ColorChoice choice = orderPrompt();
        assertThat(choice).as("the creature's controller is asked to order the preventions").isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, choice.options().stream()
                .filter(option -> option.startsWith(PERMANENT_SHIELD))
                .findFirst().orElseThrow());
        assertThat(gd.permanentDamagePreventionShield(creature.getId())).isEqualTo(shieldLeft);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private void choosePlayerShieldFirst(Player player) {
        PendingInteraction.ColorChoice choice = orderPrompt();
        assertThat(choice).as("the damaged player is asked to order the effects").isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleListChoice(player, choice.options().stream()
                .filter(option -> option.startsWith("Prevent the next"))
                .findFirst().orElseThrow());
    }
}
