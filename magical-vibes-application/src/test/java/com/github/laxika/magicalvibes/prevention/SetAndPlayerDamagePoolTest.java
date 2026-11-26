package com.github.laxika.magicalvibes.prevention;

import com.github.laxika.magicalvibes.cards.a.AegisOfHonor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.p.Progenitus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
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
 * Set damage (each matching permanent, the permanents a target player controls, the defending player's creatures,
 * each target, mass damage) and damage to players leave protection and every prevention to the damaged object's or
 * player's damage pool, so its controller orders two of them (CR 616.1; protection is a prevention, CR 615.1,
 * 702.16e), and name no face-down source (CR 708.2).
 */
@CardUsed({AegisOfHonor.class, GrizzlyBears.class, HillGiant.class, ProdigalPyromancer.class, Progenitus.class,
        Shock.class, SilverKnight.class})
class SetAndPlayerDamagePoolTest extends BaseCardTest {

    private static final String PERMANENT_SHIELD = "Prevent the next damage to it";
    private static final PermanentIsCreaturePredicate CREATURE = new PermanentIsCreaturePredicate();
    private static final CardEffect DEFENDING_CREATURES =
            DealDamageEffect.toEach(Subjects.controlledBy(Subjects.defendingPlayer(), CREATURE), 2);

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    @Nested
    @DisplayName("Protection from the source's quality")
    class ProtectionPrevents {

        @Test
        @DisplayName("Prevents a red ability's damage to each creature")
        void eachMatching() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            redAbility(null, DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), CREATURE), 2));
            assertThat(knight.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Prevents a red permanent's damage to each creature")
        void eachMatchingFromPermanent() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            resolve(pyromancer.getCard(), null, pyromancer.getId(),
                    DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), CREATURE), 2));
            assertThat(knight.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Prevents a red ability's damage to each creature target player controls")
        void permanentsTargetControls() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            redAbility(player2.getId(), DealDamageEffect.toEachControlledByTargetedPlayer(2, CREATURE));
            assertThat(knight.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Prevents a red ability's damage to each creature the defending player controls")
        void defendingPlayerCreatures() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            defendingPlayerAbility(null, DEFENDING_CREATURES);
            assertThat(knight.getMarkedDamage()).isZero();
        }


        @Test
        @DisplayName("Prevents a red ability's damage to each creature (mass damage)")
        void mass() {
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            redAbility(null, DealDamageEffect.toEachCreature(2));
            assertThat(knight.getMarkedDamage()).isZero();
        }
    }

    @Nested
    @DisplayName("Protection and a shield on the creature: its controller orders them")
    class ProtectionOrdered {

        @Test
        @DisplayName("Each matching permanent")
        void eachMatching() {
            Permanent knight = shieldedKnight();
            redAbility(null, DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), CREATURE), 2));
            chooseShieldFirst(knight, 3);
        }

        @Test
        @DisplayName("Each matching permanent, dealt by a red permanent")
        void eachMatchingFromPermanent() {
            Permanent knight = shieldedKnight();
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            resolve(pyromancer.getCard(), null, pyromancer.getId(),
                    DealDamageEffect.toEach(Subjects.controlledBy(Subjects.eachPlayer(), CREATURE), 2));
            chooseShieldFirst(knight, 3);
        }

        @Test
        @DisplayName("The permanents target player controls")
        void permanentsTargetControls() {
            Permanent knight = shieldedKnight();
            redAbility(player2.getId(), DealDamageEffect.toEachControlledByTargetedPlayer(2, CREATURE));
            chooseShieldFirst(knight, 3);
        }

        @Test
        @DisplayName("The defending player's creatures")
        void defendingPlayerCreatures() {
            Permanent knight = shieldedKnight();
            defendingPlayerAbility(null, DEFENDING_CREATURES);
            chooseShieldFirst(knight, 3);
        }


        @Test
        @DisplayName("Mass damage")
        void mass() {
            Permanent knight = shieldedKnight();
            redAbility(null, DealDamageEffect.toEachCreature(2));
            chooseShieldFirst(knight, 3);
        }
    }

    /** A target with protection is an illegal target (CR 702.16b, 608.2b), so each target is shown with a source prevention. */
    @Nested
    @DisplayName("Each target, with a prevention of damage from red sources")
    class EachTarget {

        @Test
        @DisplayName("Prevents a red ability's damage to its target creature")
        void prevents() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            redAbility(bears.getId(), DealDamageEffect.toEachTarget(new Fixed(2)));
            assertThat(bears.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("The prevention and a shield on the creature: its controller orders them")
        void ordered() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            gd.addPermanentDamagePreventionShield(bears.getId(), 5);
            redAbility(bears.getId(), DealDamageEffect.toEachTarget(new Fixed(2)));
            chooseShieldFirst(bears, 3);
        }
    }

    @Nested
    @DisplayName("Damage to players")
    class Players {

        @Test
        @DisplayName("A red-source prevention prevents a red ability's damage to each opponent")
        void sourcePreventionPrevents() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            redAbility(null, DealDamageEffect.to(Subjects.eachOpponent(), 2));
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A red-source prevention and the opponent's shield: the opponent orders them")
        void sourcePreventionAndShieldEachOpponent() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);
            redAbility(null, DealDamageEffect.to(Subjects.eachOpponent(), 2));
            choosePlayerShieldFirst(player2);
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("A red-source prevention and the controller's shield: \"deals 2 damage to you\" asks the controller")
        void sourcePreventionAndShieldYou() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            gd.addPlayerDamagePreventionShield(player1.getId(), 5);
            redAbility(null, DealDamageEffect.to(Subjects.you(), 2));
            choosePlayerShieldFirst(player1);
            assertThat(gd.playerDamagePreventionShields().get(player1.getId())).isEqualTo(3);
            harness.assertLife(player1, 20);
        }

        @Test
        @DisplayName("A red-source prevention and the player's shield: \"that player or a planeswalker they control\" asks the player")
        void sourcePreventionAndShieldPlayerOrTheirPlaneswalker() {
            Prevention.add(gd, PreventDamageEffect.fromColors(Set.of(CardColor.RED)));
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);
            redAbility(player2.getId(), DealDamageEffect.toPlayerOrTheirPlaneswalker(2, Subjects.targetPlayer()));
            choosePlayerShieldFirst(player2);
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
        }
    }

    @Nested
    @DisplayName("Aegis of Honor")
    class Aegis {

        @Test
        @DisplayName("Redirects an instant's damage to its caster")
        void redirects() {
            activateAegis();
            castShock(player2.getId());
            harness.assertLife(player2, 20);
            harness.assertLife(player1, 18);
        }

        @Test
        @DisplayName("Aegis and a prevention shield: the damaged player orders them, the shield first leaving the Aegis unused")
        void orderedBesideAShield() {
            activateAegis();
            gd.addPlayerDamagePreventionShield(player2.getId(), 5);
            castShock(player2.getId());

            choosePlayerShieldFirst(player2);
            assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(3);
            harness.assertLife(player2, 20);
            harness.assertLife(player1, 20);
        }

        private void activateAegis() {
            Permanent aegis = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.ensurePriority(player2);
            harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(aegis), null, null);
            harness.passBothPriorities();
            harness.forceActivePlayer(player1);
            harness.clearPriorityPassed();
        }

        private void castShock(UUID targetId) {
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.ensurePriority(player1);
            harness.castInstant(player1, 0, targetId);
            harness.passBothPriorities();
        }
    }

    @Nested
    @DisplayName("A face-down source's damage prevented by protection")
    class FaceDownSource {

        @Test
        @DisplayName("Names no hidden card for the permanents target player controls")
        void permanentsTargetControls() {
            Permanent hidden = faceDownGiant();
            Permanent progenitus = addCreatureReady(player2, new Progenitus());
            resolve(hidden.getCard(), player2.getId(), hidden.getId(),
                    DealDamageEffect.toEachControlledByTargetedPlayer(2, CREATURE));
            assertHiddenAndPrevented(progenitus);
        }

        @Test
        @DisplayName("Names no hidden card for the defending player's creatures")
        void defendingPlayerCreatures() {
            Permanent hidden = faceDownGiant();
            Permanent progenitus = addCreatureReady(player2, new Progenitus());
            defendingPlayerAbility(hidden, DEFENDING_CREATURES);
            assertHiddenAndPrevented(progenitus);
        }

        private Permanent faceDownGiant() {
            Permanent hidden = addCreatureReady(player1, new HillGiant());
            hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
            return hidden;
        }

        private void assertHiddenAndPrevented(Permanent progenitus) {
            assertThat(progenitus.getMarkedDamage()).isZero();
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains("Hill Giant"));
        }
    }

    /** {@code effect} as an attack trigger of {@code dealer} ({@code null}: a red ability) with player 2 defending. */
    private void defendingPlayerAbility(Permanent dealer, CardEffect effect) {
        StackEntry entry = TestStackDoor.triggered(dealer == null ? new Shock() : dealer.getCard(), player1.getId(),
                "Set damage", List.of(effect), null, dealer == null ? null : dealer.getId());
        entry.setAttackedTargetId(player2.getId());
        entry.setDefendingPlayerId(player2.getId());
        gd.stack.add(entry);
        harness.passBothPriorities();
    }

    private void redAbility(UUID targetId, CardEffect effect) {
        resolve(new Shock(), targetId, null, effect);
    }

    private StackEntry resolve(Card card, UUID targetId, UUID sourcePermanentId, CardEffect... effects) {
        StackEntry entry = TestStackDoor.triggered(card, player1.getId(), "Set damage", List.of(effects), targetId,
                sourcePermanentId);
        gd.stack.add(entry);
        harness.passBothPriorities();
        return entry;
    }

    private Permanent shieldedKnight() {
        Permanent knight = addCreatureReady(player2, new SilverKnight());
        gd.addPermanentDamagePreventionShield(knight.getId(), 5);
        return knight;
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
