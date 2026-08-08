package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HauntingMiseryTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Exiling creature cards sets X and deals that much damage to target player")
    void dealsDamageEqualToExiledCreatureCount() {
        harness.setGraveyard(player1, List.of(new RagingGoblin(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.setLife(player2, 20);
        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0, 1));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Exiling zero cards deals no damage")
    void zeroExilesDealsNoDamage() {
        harness.setGraveyard(player1, List.of(new RagingGoblin()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        harness.setLife(player2, 20);
        harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Non-creature cards can't be exiled for the additional cost")
    void cannotExileNonCreatureCards() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new HauntingMisery()));
        giveMana();

        assertThatThrownBy(() ->
                harness.castInstantWithMultipleGraveyardExile(player1, 0, player2.getId(), List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
