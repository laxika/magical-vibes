package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IngeniousInfiltrator.class, GrizzlyBears.class, Forest.class})
class IngeniousInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Ninja you control deals combat damage to a player")
    void drawsWhenNinjaDealsCombatDamage() {
        Permanent infiltrator = addCreatureReady(player1, new IngeniousInfiltrator());
        infiltrator.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
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
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Ingenious Infiltrator onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IngeniousInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.DECLARE_BLOCKERS));
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent infiltrator = findPermanent(player1, "Ingenious Infiltrator");
        assertThat(infiltrator.isTapped()).isTrue();
        assertThat(infiltrator.isAttacking()).isTrue();
        assertThat(infiltrator.getAttackTarget()).isEqualTo(player2.getId());
    }
}
