package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KrenkoMobBossTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Goblin token per Goblin controlled, counting himself")
    void createsTokensEqualToGoblinCount() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        var tokens = findPermanents(player1, "Goblin");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(p -> {
            assertThat(p.getCard().getPower()).isEqualTo(1);
            assertThat(p.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Goblins an opponent controls are not counted")
    void ignoresOpponentGoblins() {
        Permanent krenko = addCreatureReady(player1, new KrenkoMobBoss());
        addCreatureReady(player2, new RagingGoblin());
        addCreatureReady(player2, new RagingGoblin());

        harness.activateAbility(player1, indexOf(krenko), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).hasSize(1);
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
