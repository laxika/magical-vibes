package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmovableRod.class, GrizzlyBears.class, SolRing.class, VoltaicKey.class,
        IndomitableMight.class, BeastWithin.class})
class ImmovableRodTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Immovable Rod makes its controller venture into the dungeon")
    void untappingVentureIntoDungeon() {
        Permanent rod = addReadyRod(player1);
        rod.tap();

        runUntapStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(rod.isTapped()).isFalse();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("The activated ability removes abilities and prevents attacking and blocking")
    void activatedAbilityLocksTargetPermanent() {
        Permanent rod = addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isTrue();
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The activated ability's restriction ends when Immovable Rod untaps")
    void restrictionEndsWhenRodUntaps() {
        addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        runUntapStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The activated ability cannot target Immovable Rod itself")
    void cannotTargetItself() {
        Permanent rod = addReadyRod(player1);
        addRodMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rod.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another permanent");
    }

    @Test
    void mayKeepRodTappedWithoutVenturing() {
        Permanent rod = addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rod.isTapped()).isTrue();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThatThrownBy(() -> declareAttack(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void lockedCreatureCannotBlock() {
        addReadyRod(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addRodMana();
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void noncreatureLosesManaAbilityUntilRodUntaps() {
        Permanent rod = addReadyRod(player1);
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        addRodMana();
        harness.activateAbility(player1, 0, null, ring.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ring.isTapped()).isFalse();

        runUntapStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isFalse();
        assertThatCode(() -> harness.activateAbility(player2, 0, null, null)).doesNotThrowAnyException();
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    void auraLosingEnchantGoesToGraveyard() {
        addReadyRod(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new IndomitableMight());
        aura.setAttachedTo(creature.getId());
        addRodMana();

        harness.activateAbility(player1, 0, null, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(aura.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void rodWithoutAbilitiesMustUntapNormally() {
        addReadyRod(player1);
        Permanent targetRod = addReadyRod(player2);
        targetRod.tap();
        addRodMana();
        harness.activateAbility(player1, 0, null, targetRod.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);

        assertThat(targetRod.isTapped()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void rodWithoutAbilitiesDoesNotVentureWhenUntapped() {
        addReadyRod(player1);
        Permanent targetRod = addReadyRod(player2);
        targetRod.tap();
        harness.addToBattlefield(player2, new VoltaicKey());
        addRodMana();
        harness.activateAbility(player1, 0, null, targetRod.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 1, null, targetRod.getId());
        harness.passBothPriorities();

        assertThat(targetRod.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void effectDoesNotBeginIfRodUntapsBeforeResolution() {
        Permanent rod = addReadyRod(player1);
        harness.addToBattlefield(player1, new VoltaicKey());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        addRodMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, ring.getId());
        harness.activateAbility(player1, 1, null, rod.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isFalse();
        assertThatCode(() -> harness.activateAbility(player2, 0, null, null)).doesNotThrowAnyException();
        assertThat(ring.isTapped()).isTrue();
    }

    @Test
    void restrictionEndsWhenRodLeavesBattlefield() {
        Permanent rod = addReadyRod(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, rod.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rod);
        assertThatCode(() -> declareAttack(target)).doesNotThrowAnyException();
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void tappingRodAgainDoesNotRestoreOldRestriction() {
        Permanent rod = addReadyRod(player1);
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        addRodMana();
        harness.activateAbility(player1, 0, null, firstTarget.getId());
        harness.passBothPriorities();
        runUntapStep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();
        addRodMana();

        harness.activateAbility(player1, 0, null, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(rod.isTapped()).isTrue();
        assertThatCode(() -> declareAttack(firstTarget)).doesNotThrowAnyException();
    }

    private Permanent addReadyRod(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ImmovableRod());
    }

    private Permanent addCreatureReady(Player player, GrizzlyBears card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }

    private void addRodMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void declareAttack(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        gs.declareAttackers(gd, player2, List.of(index));
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(untappingPlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(untappingPlayer, true);
    }
}
