package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HeiBaiSpiritOfBalance;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoggySwampSpiritKeeper.class, HeiBaiSpiritOfBalance.class})
class FoggySwampSpiritKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Spirit token on its controller's second draw, but not on later draws")
    void createsTokenOnSecondDraw() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));

        draw(player1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);

        draw(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Spirit tokens cannot be blocked by non-Spirit creatures")
    void spiritTokenCannotBeBlockedByNonSpirit() {
        Permanent token = createSpiritToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FoggySwampSpiritKeeper());

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, token)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Spirit tokens can be blocked by Spirit creatures")
    void spiritTokenCanBeBlockedBySpirit() {
        Permanent token = createSpiritToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HeiBaiSpiritOfBalance());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, token))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spirit tokens can block Spirit creatures")
    void spiritTokenCanBlockSpirit() {
        Permanent token = createSpiritToken();
        Permanent attacker = addCreatureReady(player2, new HeiBaiSpiritOfBalance());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, token), indexOf(player2, attacker))));

        assertThat(token.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spirit tokens cannot block non-Spirit creatures")
    void spiritTokenCannotBlockNonSpirit() {
        Permanent token = createSpiritToken();
        Permanent attacker = addCreatureReady(player2, new FoggySwampSpiritKeeper());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, token), indexOf(player2, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("An opponent's second draw does not create a token")
    void opponentSecondDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player2, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The controller's second draw also triggers during an opponent's turn")
    void secondDrawOnOpponentTurnTriggers() {
        harness.forceActivePlayer(player2);
        createSpiritToken();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A draw before the keeper entered counts toward the second draw")
    void earlierDrawCounts() {
        harness.setLibrary(player1, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));
        draw(player1);
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        draw(player1);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Each keeper triggers independently on the second draw")
    void multipleKeepersTrigger() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));
        draw(player1);
        draw(player1);

        assertThat(gd.stack).hasSize(2);
        resolveTopOfStack();
        resolveTopOfStack();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("The keeper gains life from combat damage")
    void keeperHasLifelinkInCombat() {
        Permanent keeper = addCreatureReady(player1, new FoggySwampSpiritKeeper());
        keeper.setAttacking(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The token does not inherit the keeper's lifelink")
    void tokenDoesNotHaveLifelink() {
        Permanent token = createSpiritToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The second-draw trigger resolves after the keeper leaves the battlefield")
    void triggerSurvivesKeeperLeaving() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));
        draw(player1);
        draw(player1);
        gd.playerBattlefields.get(player1.getId()).remove(keeper);
        resolveTopOfStack();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    private Permanent createSpiritToken() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new FoggySwampSpiritKeeper(), new FoggySwampSpiritKeeper()));
        draw(player1);
        draw(player1);
        resolveTopOfStack();
        return findPermanent(player1, "Spirit");
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
