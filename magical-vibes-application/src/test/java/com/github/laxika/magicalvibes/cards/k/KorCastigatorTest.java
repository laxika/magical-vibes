package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorCastigator.class, GrizzlyBears.class})
class KorCastigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Kor Castigator can't be blocked by an Eldrazi Scion")
    void cannotBeBlockedByEldraziScion() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, creatureToken("Eldrazi Scion", CardSubtype.ELDRAZI,
                CardSubtype.SCION));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Kor Castigator can be blocked by a creature that is not an Eldrazi Scion")
    void canBeBlockedByNonEldraziCreature() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Kor Castigator can be blocked by a Scion that is not an Eldrazi")
    void canBeBlockedByNonEldraziScion() {
        Permanent attacker = addAttacker();
        Permanent blocker = addCreatureReady(player2, creatureToken("Scion", CardSubtype.SCION));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new KorCastigator());
        attacker.setAttacking(true);
        return attacker;
    }

    private Card creatureToken(String name, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setToken(true);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
