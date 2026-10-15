package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GideonBattleForged;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.k.KytheonHeroOfAkros;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.y.YevasForcemage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchangelOfTithes.class, YevasForcemage.class, KytheonHeroOfAkros.class,
        GideonBattleForged.class, TurnToFrog.class})
class ArchangelOfTithesTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped, it taxes each attacking creature {1}")
    void untappedTaxesAttackers() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Untapped, attacking without the mana to pay the tax is illegal")
    void untappedBlocksUnpaidAttack() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        addReadyCreature(player2);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    @DisplayName("Tapped, it does not tax attackers at all")
    void tappedDoesNotTaxAttackers() {
        harness.addToBattlefieldAndReturn(player1, new ArchangelOfTithes()).tap();
        addReadyCreature(player2);

        // With no mana at all — untapped, the same declaration throws (see untappedBlocksUnpaidAttack).
        assertThatCode(() -> declareAttackers(player2, List.of(0))).doesNotThrowAnyException();
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
        Permanent archangel = harness.addToBattlefieldAndReturn(player1, new ArchangelOfTithes());
        archangel.setSummoningSick(false);
        archangel.setAttacking(true);

        Permanent ground = harness.addToBattlefieldAndReturn(player1, new YevasForcemage());
        ground.setSummoningSick(false);
        ground.setAttacking(true);
        return gd.playerBattlefields.get(player1.getId()).indexOf(ground);
    }

    @Test
    @DisplayName("When it is not attacking, blocking is free")
    void notAttackingLeavesBlockingFree() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new YevasForcemage());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2);

        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareBlockers(List.of(new BlockerAssignment(0, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void multipleUntappedArchangelsAddTheirAttackTaxes() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void attackingTaxesEveryBlocker() {
        int attackerIdx = setUpArchangelAttackingAlongsideGroundCreature();
        addReadyCreature(player2);
        addReadyCreature(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareBlockers(List.of(new BlockerAssignment(0, attackerIdx),
                new BlockerAssignment(1, attackerIdx)));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void losingAbilitiesRemovesAttackTax() {
        Permanent archangel = harness.addToBattlefieldAndReturn(player1, new ArchangelOfTithes());
        addReadyCreature(player2);
        turnToFrog(archangel);

        assertThatCode(() -> declareAttackers(player2, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    void losingAbilitiesWhileAttackingRemovesBlockTax() {
        int attackerIdx = setUpArchangelAttackingAlongsideGroundCreature();
        Permanent archangel = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent blocker = addReadyCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        turnToFrog(archangel);

        assertThatCode(() -> declareBlockers(List.of(new BlockerAssignment(0, attackerIdx))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    void attackingABattleDoesNotRequirePayment() {
        harness.addToBattlefield(player1, new ArchangelOfTithes());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        addReadyCreature(player2);

        assertThatCode(() -> declareAttackers(player2, List.of(1), Map.of(1, battle.getId())))
                .doesNotThrowAnyException();
    }

    private void turnToFrog(Permanent archangel) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, archangel.getId());
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new YevasForcemage());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player,
                new KytheonHeroOfAkros().getBackFaceCard());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        return planeswalker;
    }
}
