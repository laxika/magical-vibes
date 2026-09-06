package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FemerefKnight;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GoblinScouts.class, FemerefKnight.class, Mountain.class})
class GoblinScoutsTest extends BaseCardTest {

    private List<Permanent> scoutTokens(Player player) {
        return findPermanents(player, "Goblin Scout").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void cast() {
        harness.castFromHand(player1, new GoblinScouts(), "{3}{R}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving creates three 1/1 red Goblin Scout tokens with mountainwalk")
    void resolvingCreatesThreeTokens() {
        cast();

        List<Permanent> tokens = scoutTokens(player1);
        assertThat(tokens).hasSize(3);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.SCOUT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.MOUNTAINWALK);
        }
    }

    @Test
    @DisplayName("Tokens enter only under the caster's control")
    void tokensEnterUnderControllerControl() {
        cast();

        assertThat(scoutTokens(player1)).hasSize(3);
        assertThat(scoutTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("Goblin Scouts goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        cast();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Goblin Scouts");
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking while the defending player controls a Mountain")
    void mountainwalkPreventsBlockingWithMountain() {
        cast();

        Permanent attacker = scoutTokens(player1).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new FemerefKnight());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountainwalk permits blocking while the defending player controls no Mountain")
    void mountainwalkPermitsBlockingWithoutMountain() {
        cast();

        Permanent attacker = scoutTokens(player1).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefKnight());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
