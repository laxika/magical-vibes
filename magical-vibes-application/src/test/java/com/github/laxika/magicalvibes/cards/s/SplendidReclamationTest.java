package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SplendidReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all lands from your graveyard to the battlefield tapped")
    void returnsAllLandsFromYourGraveyardTapped() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new GrizzlyBears();
        Card opponentMountain = new Mountain();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest, island, creature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentMountain)));

        castSplendidReclamation();

        assertThat(battlefieldCards(player1)).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(forest, island);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentMountain);
        assertThat(battlefieldCards(player2)).isEmpty();
    }

    private void castSplendidReclamation() {
        harness.setHand(player1, List.of(new SplendidReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> battlefieldCards(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .toList();
    }
}
