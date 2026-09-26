package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SqueeDubiousMonarch.class, GrizzlyBears.class})
class SqueeDubiousMonarchTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Goblin token")
    void attackingCreatesGoblinToken() {
        addCreatureReady(player1, new SqueeDubiousMonarch());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent goblin = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttackedThisTurn()).isTrue();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Can be cast from the graveyard for {3}{R} by exiling four other cards")
    void castFromGraveyardExilesFourOtherCards() {
        SqueeDubiousMonarch squee = new SqueeDubiousMonarch();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(squee, first, second, third, fourth));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Squee, Dubious Monarch");
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard without four other cards")
    void requiresFourOtherCardsToCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(
                new SqueeDubiousMonarch(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
