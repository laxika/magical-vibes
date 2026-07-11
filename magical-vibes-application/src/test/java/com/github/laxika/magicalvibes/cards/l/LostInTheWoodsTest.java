package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LostInTheWoodsTest extends BaseCardTest {

    /** Puts an attacking creature on player2's battlefield and Lost in the Woods on player1's. */
    private Permanent setUpAttackAgainstLostInTheWoods() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LostInTheWoods()));

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("Attacking the controller triggers Lost in the Woods on the defender's side")
    void attackTriggersAbility() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Forest())));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Lost in the Woods");
        // The trigger is controlled by the defending player, not the attacker
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        // The attacking creature is recorded so the effect can remove it from combat
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Revealing a Forest removes the attacking creature from combat and bottoms the card")
    void forestRemovesAttackerFromCombat() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Forest(), new Island())));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        gs.declareAttackers(gd, player2, List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        // Resolve only the trigger (not the whole turn, which would end combat anyway)
        harness.getStackResolutionService().resolveTopOfStack(gd);

        // Forest revealed -> attacker removed from combat
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();

        // Revealed Forest went to the bottom of the library
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo("Island");
        assertThat(deck.getLast().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Revealing a non-Forest leaves the attacker in combat and bottoms the card")
    void nonForestLeavesAttackerInCombat() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Island(), new Forest())));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        gs.declareAttackers(gd, player2, List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        // Resolve only the trigger (not the whole turn, which would end combat anyway)
        harness.getStackResolutionService().resolveTopOfStack(gd);

        // Island revealed -> attacker stays in combat
        assertThat(attacker.isAttacking()).isTrue();

        // Revealed Island went to the bottom of the library
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo("Forest");
        assertThat(deck.getLast().getName()).isEqualTo("Island");
    }

    @Test
    @DisplayName("Fires once per attacking creature")
    void firesOncePerAttacker() {
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(new Island(), new Forest())));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LostInTheWoods()));

        Permanent attacker1 = new Permanent(new GrizzlyBears());
        attacker1.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker1);
        Permanent attacker2 = new Permanent(new GrizzlyBears());
        attacker2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(attacker2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Lost in the Woods"));
    }
}
