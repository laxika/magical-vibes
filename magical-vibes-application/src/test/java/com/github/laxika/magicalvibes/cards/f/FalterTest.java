package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelicPage;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.z.Zephid;
import com.github.laxika.magicalvibes.cards.z.ZephidsEmbrace;
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

@CardUsed({Falter.class, GorillaWarrior.class, Zephid.class, AngelicPage.class,
        ZephidsEmbrace.class, Humble.class})
class FalterTest extends BaseCardTest {

    @Test
    @DisplayName("A creature without flying can't block this turn")
    void creatureWithoutFlyingCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());

        castFalter();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with flying can still block this turn")
    void creatureWithFlyingCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent blocker = addCreatureReady(player2, new Zephid());

        castFalter();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("It affects creatures controlled by either player")
    void affectsBothPlayers() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent ownGroundCreature = addCreatureReady(player1, new GorillaWarrior());
        Permanent opponentGroundCreature = addCreatureReady(player2, new GorillaWarrior());

        castFalter();

        assertThat(ownGroundCreature.isCantBlockThisTurn()).isTrue();
        assertThat(opponentGroundCreature.isCantBlockThisTurn()).isTrue();
        assertThat(attacker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A creature that gains flying after Falter resolves can block this turn")
    void creatureThatGainsFlyingAfterFalterResolvesCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());

        castFalter();
        castZephidsEmbrace(blocker);

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FLYING)).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature that loses flying after Falter resolves still can't block this turn")
    void creatureThatLosesFlyingAfterFalterResolvesCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        Permanent blocker = addCreatureReady(player2, new AngelicPage());

        castFalter();
        castHumble(blocker);

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FLYING)).isFalse();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature entering after Falter resolves can't block this turn")
    void creatureEnteringAfterFalterResolvesCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());

        castFalter();
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The restriction wears off at the end of the turn")
    void restrictionWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GorillaWarrior());

        castFalter();

        assertThat(creature.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.isCantBlockThisTurn()).isFalse();
    }

    private void castFalter() {
        harness.castFromHand(player1, new Falter(), "{1}{R}");
        harness.passBothPriorities();
    }

    private void castZephidsEmbrace(Permanent target) {
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castHumble(Permanent target) {
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
