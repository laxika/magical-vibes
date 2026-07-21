package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OvercomeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Overcome gives own creatures +2/+2 and trample")
    void resolvesAndBuffsOwnCreatures() {
        Permanent p1a = addReadyCreature(player1, new GrizzlyBears());
        Permanent p1b = addReadyCreature(player1, new GrizzlyBears());
        Permanent p2 = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overcome()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(p1a.getEffectivePower()).isEqualTo(4);
        assertThat(p1a.getEffectiveToughness()).isEqualTo(4);
        assertThat(p1b.getEffectivePower()).isEqualTo(4);
        assertThat(p1b.getEffectiveToughness()).isEqualTo(4);
        assertThat(p1a.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p2.getEffectivePower()).isEqualTo(2);
        assertThat(p2.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overcome trample assigns excess damage to defending player")
    void trampleAssignsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overcome()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 4/4 trample blocked by 2/2 → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Overcome effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overcome()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting Overcome puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Overcome()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Overcome");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
