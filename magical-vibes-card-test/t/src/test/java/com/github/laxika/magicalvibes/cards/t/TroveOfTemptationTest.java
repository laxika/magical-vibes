package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TroveOfTemptation.class, TishanasWayfinder.class, JaceCunningCastaway.class,
        Propaganda.class, InvasionOfZendikar.class, AwakenedSkyclave.class})
class TroveOfTemptationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts enchantment on stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new TroveOfTemptation(), "{3}{R}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(TroveOfTemptation.class);
    }

    @Test
    @DisplayName("Opponent must attack with at least one creature when Trove is on the battlefield")
    void opponentMustAttackWithAtLeastOneCreature() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Declaring no attackers should fail
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must attack with at least one creature");
    }

    @Test
    @DisplayName("Opponent can successfully declare one creature as attacker")
    void opponentCanDeclareOneAttacker() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Only requires one creature to attack even when multiple are available")
    void onlyOneCreatureRequiredToAttack() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent wayfinder1 = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder1.setSummoningSick(false);

        Permanent wayfinder2 = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder2.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Declaring just one attacker should succeed (unlike Curse of Nightly Hunt which requires ALL)
        gs.declareAttackers(gd, player2, List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Controller's creatures are not forced to attack")
    void controllerNotForced() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent wayfinder = harness.addToBattlefieldAndReturn(player1, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Controller's creatures should NOT be forced
        gs.declareAttackers(gd, player1, List.of());
    }

    @Test
    @DisplayName("No creature forced to attack if all are tapped")
    void noForceIfAllTapped() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);
        wayfinder.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of());

        assertThat(wayfinder.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Effect removed when Trove leaves the battlefield")
    void effectRemovedWhenTroveLeaves() {
        TroveOfTemptation trove = new TroveOfTemptation();
        harness.addToBattlefield(player1, trove);

        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        // Remove Trove from battlefield
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() == trove);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Now opponent can choose not to attack
        gs.declareAttackers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Creates Treasure token at controller's end step")
    void createsTreasureTokenAtEndStep() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        advanceToEndStep(player1);

        // Treasure token trigger should be on the stack
        assertThat(gd.stack).anySatisfy(entry ->
                assertThat(entry.getCard().getName()).isEqualTo("Trove of Temptation"));

        harness.passBothPriorities();

        // Treasure token should be on the battlefield
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anySatisfy(p -> {
            assertThat(p.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(p.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        });
    }

    @Test
    @DisplayName("Does not create Treasure on opponent's end step")
    void noTreasureOnOpponentEndStep() {
        harness.addToBattlefield(player1, new TroveOfTemptation());

        int bfSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        advanceToEndStep(player2);

        // No trigger should fire — stack should not contain Trove trigger
        assertThat(gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Trove of Temptation"))
                .count()).isZero();

        // Battlefield should not have gained any permanents
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(bfSizeBefore);
    }

    @Test
    @DisplayName("A battle attack does not satisfy the requirement to attack Trove's controller or a planeswalker")
    void attackingOnlyABattleDoesNotSatisfyRequirement() {
        harness.addToBattlefield(player1, new TroveOfTemptation());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(1),
                Map.of(1, battle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attack tax on the player does not waive a free attack on their planeswalker")
    void mustAttackWhenPlaneswalkerCanBeAttackedWithoutPaying() {
        harness.addToBattlefield(player1, new TroveOfTemptation());
        harness.addToBattlefield(player1, new Propaganda());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent may decline to pay an attack tax when no planeswalker is available")
    void mayDeclineAttackTax() {
        harness.addToBattlefield(player1, new TroveOfTemptation());
        harness.addToBattlefield(player1, new Propaganda());
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of());
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An end-step trigger creates exactly one Treasure even if Trove leaves before resolution")
    void treasureTriggerSurvivesSourceLeavingBattlefield() {
        Permanent trove = harness.addToBattlefieldAndReturn(player1, new TroveOfTemptation());
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(trove);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Attacking the controller's planeswalker satisfies Trove without paying Propaganda")
    void attackingPlaneswalkerSatisfiesRequirement() {
        harness.addToBattlefield(player1, new TroveOfTemptation());
        harness.addToBattlefield(player1, new Propaganda());
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceCunningCastaway());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent wayfinder = harness.addToBattlefieldAndReturn(player2, new TishanasWayfinder());
        wayfinder.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, jace.getId()));

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The created Treasure can tap and sacrifice for one mana of any color")
    void treasureCanProduceAnyColor(ManaColor color) {
        harness.addToBattlefield(player1, new TroveOfTemptation());
        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
