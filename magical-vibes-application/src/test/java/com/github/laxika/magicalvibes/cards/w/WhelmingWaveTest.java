package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhelmingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns non-exempt creatures to their owners' hands")
    void returnsNonExemptCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent kraken = addCreatureWithSubtype(player1, CardSubtype.KRAKEN);
        Permanent leviathan = addCreatureWithSubtype(player1, CardSubtype.LEVIATHAN);
        Permanent octopus = addCreatureWithSubtype(player2, CardSubtype.OCTOPUS);
        Permanent serpent = addCreatureWithSubtype(player2, CardSubtype.SERPENT);

        harness.setHand(player1, List.of(new WhelmingWave()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactlyInAnyOrder(kraken, leviathan);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactlyInAnyOrder(octopus, serpent);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getId())
                .contains(ownCreature.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(opponentCreature.getCard().getId());
    }

    private Permanent addCreatureWithSubtype(Player player, CardSubtype subtype) {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(subtype));
        return addCreatureReady(player, card);
    }
}
