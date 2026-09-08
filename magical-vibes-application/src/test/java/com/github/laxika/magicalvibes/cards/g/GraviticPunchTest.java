package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraviticPunchTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals damage equal to its power to a target player")
    void creatureDamagesTargetPlayer() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(giantId, player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target a creature as the player target")
    void cannotTargetCreatureAsPlayer() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GraviticPunch()));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(giantId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("Jump-start discards a card, deals damage, and exiles the spell")
    void jumpStartDiscardsDealsDamageAndExiles() {
        GraviticPunch spell = new GraviticPunch();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        gs.playFlashbackSpell(gd, player1, 0, null, null,
                List.of(giantId, player2.getId()), null, null, List.of(), 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
