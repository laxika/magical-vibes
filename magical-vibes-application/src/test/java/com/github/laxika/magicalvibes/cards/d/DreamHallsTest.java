package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DreamHallsTest extends BaseCardTest {

    @Test
    @DisplayName("A spell can be cast by discarding a card that shares its color")
    void castsByDiscardingSharedColor() {
        harness.addToBattlefield(player1, new DreamHalls());
        Divination spell = new Divination();
        Opt discarded = new Opt();
        harness.setHand(player1, List.of(spell, discarded));

        harness.castSorceryWithSharedColorDiscard(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The discard alternative is available to an opponent")
    void opponentCanUseDreamHalls() {
        harness.addToBattlefield(player1, new DreamHalls());
        Divination spell = new Divination();
        Opt discarded = new Opt();
        harness.setHand(player2, List.of(spell, discarded));
        harness.forceActivePlayer(player2);

        harness.castSorceryWithSharedColorDiscard(player2, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("A card with a different color cannot pay for the spell")
    void rejectsDifferentColor() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Divination(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castSorceryWithSharedColorDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell still pays mana normally when the discard alternative is not selected")
    void canPayManaNormally() {
        harness.addToBattlefield(player1, new DreamHalls());
        harness.setHand(player1, List.of(new Divination(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The spell's own additional discard cost is paid separately")
    void paysAdditionalDiscardCostSeparately() {
        harness.addToBattlefield(player1, new DreamHalls());
        TormentingVoice spell = new TormentingVoice();
        Shock sharedDiscard = new Shock();
        Shock additionalDiscard = new Shock();
        harness.setHand(player1, List.of(spell, sharedDiscard, additionalDiscard));

        harness.castSorceryWithSharedColorDiscardAndDiscard(player1, 0, 1, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sharedDiscard, additionalDiscard);
    }
}
