package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({FoggySwampSpiritKeeper.class, GrizzlyBears.class, InnocenceKami.class})
class FoggySwampSpiritKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Spirit token on its controller's second draw, but not on later draws")
    void createsTokenOnSecondDraw() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

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
    @DisplayName("Spirit tokens cannot block or be blocked by non-Spirit creatures")
    void spiritTokenCannotBlockOrBeBlockedByNonSpirit() {
        Permanent token = createSpiritToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears());

        assertThat(bls.canBlock(gd, token)).isFalse();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, token)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Spirit tokens can be blocked by Spirit creatures")
    void spiritTokenCanBeBlockedBySpirit() {
        Permanent token = createSpiritToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addReadyPermanent(player2, new InnocenceKami());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, token))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent createSpiritToken() {
        harness.addToBattlefield(player1, new FoggySwampSpiritKeeper());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        draw(player1);
        resolveTopOfStack();
        return findPermanent(player1, "Spirit");
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
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
