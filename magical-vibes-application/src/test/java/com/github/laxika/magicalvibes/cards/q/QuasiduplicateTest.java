package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuasiduplicateTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target creature you control")
    void createsTokenCopyOfTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Jump-start discards a card, creates a token copy, and exiles Quasiduplicate")
    void jumpStartDiscardsCreatesTokenCopyAndExilesSpell() {
        Quasiduplicate spell = new Quasiduplicate();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castJumpStart(player1, 0, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
