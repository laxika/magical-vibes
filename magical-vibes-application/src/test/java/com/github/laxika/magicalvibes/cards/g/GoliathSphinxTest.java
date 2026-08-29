package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoliathSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Goliath Sphinx cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new GoliathSphinx());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Goliath Sphinx can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent attacker = addCreatureReady(player1, new GoliathSphinx());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, createFlyingCreature());

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private static Card createFlyingCreature() {
        Card card = new Card();
        card.setName("Wind Drake");
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}");
        card.setColor(CardColor.BLUE);
        card.setPower(2);
        card.setToughness(2);
        card.setKeywords(Set.of(Keyword.FLYING));
        return card;
    }
}
