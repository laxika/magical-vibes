package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JuggernautTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Juggernaut has correct card properties")
    void hasCorrectProperties() {
        Juggernaut card = new Juggernaut();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof MustAttackEffect);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof CanBeBlockedOnlyByFilterEffect c
                        && c.blockerPredicate().equals(new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.WALL))));
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Juggernaut puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Juggernaut()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Juggernaut"));
    }

    // ===== Must attack =====

    @Test
    @DisplayName("Juggernaut must attack each combat if able")
    void mustAttackWhenAble() {
        Permanent juggernaut = new Permanent(new Juggernaut());
        juggernaut.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Juggernaut deals 5 combat damage when unblocked")
    void dealsFiveDamageUnblocked() {
        harness.setLife(player2, 20);

        Permanent juggernaut = new Permanent(new Juggernaut());
        juggernaut.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Can't be blocked by Walls =====

    @Test
    @DisplayName("Juggernaut cannot be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent juggernaut = new Permanent(new Juggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        Permanent wall = new Permanent(new AngelicWall());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by non-Wall creatures");
    }

    @Test
    @DisplayName("Juggernaut can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent juggernaut = new Permanent(new Juggernaut());
        juggernaut.setSummoningSick(false);
        juggernaut.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(juggernaut);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }
}


