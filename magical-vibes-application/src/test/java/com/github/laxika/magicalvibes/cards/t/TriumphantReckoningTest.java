package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphantReckoning.class, AjaniGoldmane.class, GloriousAnthem.class,
        GrizzlyBears.class, Ornithopter.class})
class TriumphantReckoningTest extends BaseCardTest {

    @Test
    @DisplayName("Returns artifact, enchantment, and planeswalker cards from your graveyard")
    void returnsMatchingPermanentCardsFromOwnGraveyard() {
        Card artifact = new Ornithopter();
        Card enchantment = new GloriousAnthem();
        Card planeswalker = new AjaniGoldmane();
        Card creature = new GrizzlyBears();
        Card opponentArtifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact, enchantment, planeswalker, creature));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setHand(player1, List.of(new TriumphantReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Ajani Goldmane");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Triumphant Reckoning");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }
}
