package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChangelingOutcast;
import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IngeniousInfiltrator.class, MotherBear.class, SnowCoveredForest.class, ChangelingOutcast.class})
class IngeniousInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Ninja you control deals combat damage to a player")
    void drawsWhenNinjaDealsCombatDamage() {
        Permanent infiltrator = addCreatureReady(player1, new IngeniousInfiltrator());
        infiltrator.setAttacking(true);
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a non-Ninja creature deals combat damage")
    void ignoresNonNinjaCombatDamage() {
        addCreatureReady(player1, new IngeniousInfiltrator());
        Permanent bears = addCreatureReady(player1, new MotherBear());
        bears.setAttacking(true);
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Ingenious Infiltrator onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new MotherBear());
        addCreatureReady(player2, new MotherBear());
        declareAttackers(List.of(0));

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.setHand(player1, List.of(new IngeniousInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.activateHandAbility(player1, 0, attacker.getId());
            harness.assertInHand(player1, "Mother Bear");
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Mother Bear");
        Permanent infiltrator = findPermanent(player1, "Ingenious Infiltrator");
        assertThat(infiltrator.isTapped()).isTrue();
        assertThat(infiltrator.isAttacking()).isTrue();
        assertThat(infiltrator.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Each Ninja dealing damage triggers every Infiltrator separately")
    void drawsForEachNinjaAndEachInfiltrator() {
        Permanent first = addCreatureReady(player1, new IngeniousInfiltrator());
        Permanent second = addCreatureReady(player1, new IngeniousInfiltrator());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.setLibrary(player1, List.of(new SnowCoveredForest(), new SnowCoveredForest(),
                new SnowCoveredForest(), new SnowCoveredForest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A changeling is a Ninja for the combat damage trigger")
    void drawsForChangelingCombatDamage() {
        addCreatureReady(player1, new IngeniousInfiltrator());
        Permanent outcast = addCreatureReady(player1, new ChangelingOutcast());
        outcast.setAttacking(true);
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opposing Ninja only triggers its own controller's Infiltrator")
    void ignoresOpposingNinjaCombatDamage() {
        addCreatureReady(player1, new IngeniousInfiltrator());
        Permanent opponent = addCreatureReady(player2, new IngeniousInfiltrator());
        opponent.setAttacking(true);
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.setLibrary(player2, List.of(new SnowCoveredForest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu cannot return an attacker before the declare blockers step")
    void cannotActivateNinjutsuBeforeBlockers() {
        Permanent attacker = addCreatureReady(player1, new MotherBear());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new IngeniousInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Ingenious Infiltrator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ninjutsu requires both colored mana and does not return the attacker if payment fails")
    void cannotActivateNinjutsuWithoutBlackMana() {
        Permanent attacker = addCreatureReady(player1, new MotherBear());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new IngeniousInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        harness.assertInHand(player1, "Ingenious Infiltrator");
        assertThat(gd.stack).isEmpty();
    }
}
