package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchangelOfTithesTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped, it taxes each attacking creature {1}")
    void untappedTaxesAttackers() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0, 1), null);

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapped, attacking without the mana to pay the tax is illegal")
    void untappedBlocksUnpaidAttack() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tapped, it does not tax attackers at all")
    void tappedDoesNotTaxAttackers() {
        harness.addToBattlefieldAndReturn(player1, new ArchangelOfTithes()).tap();
        addReadyCreature(player2);

        // With no mana at all — untapped, the same declaration throws (see untappedBlocksUnpaidAttack).
        assertThatCode(() -> declareAttackers(player2, List.of(0), null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Untapped, the tax also applies to attacks on the controller's planeswalkers")
    void untappedTaxesPlaneswalkerAttacks() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        Permanent planeswalker = addPlaneswalker(player1);
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("While it attacks, each blocking creature costs its controller {1}")
    void attackingTaxesBlockers() {
        int groundAttackerIdx = setUpArchangelAttackingAlongsideGroundCreature();
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareBlockers(List.of(new BlockerAssignment(0, groundAttackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("While it attacks, blocking without the mana to pay is illegal")
    void attackingBlocksUnpaidBlock() {
        int groundAttackerIdx = setUpArchangelAttackingAlongsideGroundCreature();
        Permanent blocker = addReadyCreature(player2);

        assertThatThrownBy(() -> declareBlockers(List.of(new BlockerAssignment(0, groundAttackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("block cost");
        assertThat(blocker.isBlocking()).isFalse();
    }

    /** Archangel attacking (so the block tax is live) plus a blockable ground attacker; returns its index. */
    private int setUpArchangelAttackingAlongsideGroundCreature() {
        Permanent archangel = new Permanent(new ArchangelOfTithes());
        archangel.setSummoningSick(false);
        archangel.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(archangel);

        Permanent ground = new Permanent(new GrizzlyBears());
        ground.setSummoningSick(false);
        ground.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(ground);
        return gd.playerBattlefields.get(player1.getId()).indexOf(ground);
    }

    @Test
    @DisplayName("When it is not attacking, blocking is free")
    void notAttackingLeavesBlockingFree() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        Permanent blocker = addReadyCreature(player2);

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareBlockers(List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(4);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
