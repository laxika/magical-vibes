package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LeoninSkyhunter;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeroOfBladehold.class, LeoninSkyhunter.class})
class HeroOfBladeholdTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Hero of Bladehold creates two 1/1 Soldier tokens tapped and attacking")
    void attackCreatesTokensTappedAndAttacking() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        declareAttackers(List.of(0));

        // Stack should have: token creation trigger + battle cry trigger
        assertThat(gd.stack).hasSize(2);

        // Resolve battle cry first (it's on top of stack)
        harness.passBothPriorities();

        // Keep combat open to inspect the tokens while they are attacking.
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        // Two Soldier tokens should be on the battlefield
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        long soldierTokenCount = battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .count();
        assertThat(soldierTokenCount).isEqualTo(2);

        // Entering attacking does not count as being declared as an attacker.
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .forEach(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttacking()).isTrue();
                    assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
                    assertThat(token.isAttackedThisTurn()).isFalse();
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Hero of Bladehold attack trigger puts both triggers on stack")
    void attackPutsTriggersOnStack() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        declareAttackers(List.of(0));

        // Both token creation trigger and battle cry trigger should be on the stack
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allSatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getCard().getName()).isEqualTo("Hero of Bladehold");
        });
    }

    @Test
    @DisplayName("Battle cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        Permanent skyhunter = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        skyhunter.setSummoningSick(false);

        declareAttackers(List.of(0, 1));

        // Resolve battle cry trigger (on top of stack)
        harness.passBothPriorities();

        // Skyhunter should get +1/+0 from battle cry
        assertThat(skyhunter.getPowerModifier()).isEqualTo(1);
        assertThat(skyhunter.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Battle cry does not boost Hero of Bladehold itself")
    void battleCryDoesNotBoostSelf() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        declareAttackers(List.of(0));

        // Resolve battle cry trigger
        harness.passBothPriorities();

        // Hero should NOT get its own battle cry boost
        assertThat(hero.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Soldier tokens are white creature tokens")
    void soldierTokensAreWhiteCreatures() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        declareAttackers(List.of(0));

        // Resolve both triggers
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        battlefield.stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Soldier"))
                .forEach(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.WHITE);
                    assertThat(token.getCard().getType()).isEqualTo(com.github.laxika.magicalvibes.model.CardType.CREATURE);
                    assertThat(token.getCard().getSubtypes()).contains(com.github.laxika.magicalvibes.model.CardSubtype.SOLDIER);
                });
    }

    @Test
    @DisplayName("Controller can choose the order of the simultaneous attack triggers")
    void controllerChoosesAttackTriggerOrder() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);

        declareAttackers(List.of(0));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice)
                .as("The controller must be allowed to put battle cry below token creation")
                .isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).hasSize(2);
    }

    @Test
    @DisplayName("Battle cry does not boost creatures that are not attacking")
    void battleCryDoesNotBoostNonattackers() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);
        Permanent ownNonattacker = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LeoninSkyhunter());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(ownNonattacker.getPowerModifier()).isZero();
        assertThat(ownNonattacker.getToughnessModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attack abilities still resolve after Hero of Bladehold leaves the battlefield")
    void attackTriggersResolveWithoutHero() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);
        Permanent otherAttacker = harness.addToBattlefieldAndReturn(player1, new LeoninSkyhunter());
        otherAttacker.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        gd.playerBattlefields.get(player1.getId()).remove(hero);
        harness.passBothPriorities();
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getPowerModifier()).isZero();
        });
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Soldier tokens can attack a battle protected by the defending player")
    void tokensCanAttackDefendingPlayersBattle() {
        Permanent hero = harness.addToBattlefieldAndReturn(player1, new HeroOfBladehold());
        hero.setSummoningSick(false);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, harness::passBothPriorities);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).as("Each Soldier may attack the protected battle").isNotNull();
        assertThat(choice.validIds()).contains(battle.getId());
    }
}
