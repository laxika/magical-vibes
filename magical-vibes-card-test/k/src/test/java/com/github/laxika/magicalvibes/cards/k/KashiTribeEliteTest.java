package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BriarknitKami;
import com.github.laxika.magicalvibes.cards.g.GhostLitNourisher;
import com.github.laxika.magicalvibes.cards.i.IntoTheFray;
import com.github.laxika.magicalvibes.cards.m.MatsuTribeBirdstalker;
import com.github.laxika.magicalvibes.cards.s.Sasaya;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KashiTribeElite.class, Sasaya.class, MatsuTribeBirdstalker.class,
        IntoTheFray.class, GhostLitNourisher.class, BriarknitKami.class})
class KashiTribeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary Snakes you control have shroud")
    void grantsShroudToLegendarySnakesYouControl() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        Permanent sasaya = addCreatureReady(player1, new Sasaya());
        Permanent ordinarySnake = addCreatureReady(player1, new MatsuTribeBirdstalker());
        Permanent opposingSasaya = addCreatureReady(player2, new Sasaya());

        assertThat(gqs.hasKeyword(gd, sasaya, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, kashi, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinarySnake, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSasaya, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake")
    void cannotBeTargetedBySpells() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sasaya = addCreatureReady(player1, new Sasaya());
        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sasaya.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake with abilities")
    void cannotBeTargetedByAbilities() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sasaya = addCreatureReady(player1, new Sasaya());
        Permanent nourisher = addCreatureReady(player1, new GhostLitNourisher());
        harness.addMana(player1, ManaColor.GREEN, 3);

        int nourisherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nourisher);
        assertThatThrownBy(() -> harness.activateAbility(player1, nourisherIndex, null, sasaya.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent blocker = addCreatureReady(player2, new BriarknitKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isZero();

        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap and untap restriction are a single trigger even when the Elite dies")
    void combatDamageCreatesOneAbilityAfterSourceDies() {
        Permanent elite = addCreatureReady(player1, new KashiTribeElite());
        Permanent blocker = addCreatureReady(player2, new BriarknitKami());
        elite.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(elite);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elite.getCard());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage ability affects a legendary Snake with shroud")
    void combatDamageDoesNotTargetDamagedCreature() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent blocker = addCreatureReady(player2, new Sasaya());
        addCreatureReady(player2, new KashiTribeElite());
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.SHROUD)).isTrue();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(blocker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage dealt while blocking also locks the attacker")
    void blockingEliteLocksAttacker() {
        Permanent attacker = addCreatureReady(player1, new BriarknitKami());
        addCreatureReady(player2, new KashiTribeElite());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger the creature tap ability")
    void playerDamageDoesNotTriggerTapAbility() {
        Permanent elite = addCreatureReady(player1, new KashiTribeElite());
        elite.setAttacking(true);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud ends when the Elite leaves the battlefield")
    void shroudEndsWhenEliteLeavesBattlefield() {
        Permanent elite = addCreatureReady(player1, new KashiTribeElite());
        Permanent sasaya = addCreatureReady(player1, new Sasaya());
        assertThat(gqs.hasKeyword(gd, sasaya, Keyword.SHROUD)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(elite);
        assertThat(gqs.hasKeyword(gd, sasaya, Keyword.SHROUD)).isFalse();

        harness.setHand(player1, List.of(new IntoTheFray()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, sasaya.getId());
        assertThat(gd.stack).isEmpty();
    }
}
