package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.SorinLordOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostInTheWoods.class, GrizzlyBears.class, Forest.class, Island.class})
class LostInTheWoodsTest extends BaseCardTest {

    /** Puts an attacking creature on player2's battlefield and Lost in the Woods on player1's. */
    private Permanent setUpAttackAgainstLostInTheWoods() {
        harness.addToBattlefield(player1, new LostInTheWoods());

        return addCreatureReady(player2, new GrizzlyBears());
    }

    @Test
    @DisplayName("Attacking the controller triggers Lost in the Woods on the defender's side")
    void attackTriggersAbility() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        declareAttackers(player2, List.of(0));

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
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        declareAttackers(player2, List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        // Resolve only the trigger (not the whole turn, which would end combat anyway)
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

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
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        Permanent attacker = setUpAttackAgainstLostInTheWoods();

        declareAttackers(player2, List.of(0));
        assertThat(attacker.isAttacking()).isTrue();

        // Resolve only the trigger (not the whole turn, which would end combat anyway)
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

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
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        harness.addToBattlefield(player1, new LostInTheWoods());

        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());


        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Lost in the Woods"));
    }

    @Test
    @CardUsed({SorinLordOfInnistrad.class})
    void forestRemovesCreatureAttackingControlledPlaneswalker() {
        Permanent attacker = setUpAttackAgainstLostInTheWoods();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new SorinLordOfInnistrad());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void emptyLibraryLeavesAttackerInCombat() {
        Permanent attacker = setUpAttackAgainstLostInTheWoods();
        harness.setLibrary(player1, List.of());
        declareAttackers(player2, List.of(0));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void departedAttackerDoesNotPreventRevealAndBottom() {
        Permanent attacker = setUpAttackAgainstLostInTheWoods();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island, forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerResolvesAfterEnchantmentLeavesBattlefield() {
        Permanent attacker = setUpAttackAgainstLostInTheWoods();
        harness.setLibrary(player1, List.of(new Forest()));
        declareAttackers(player2, List.of(0));
        Permanent enchantment = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(enchantment.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({InvasionOfZendikar.class})
    void attackingControlledBattleDoesNotTrigger() {
        harness.addToBattlefield(player2, new LostInTheWoods());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player2, List.of(1), Map.of(1, battle.getId())));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
