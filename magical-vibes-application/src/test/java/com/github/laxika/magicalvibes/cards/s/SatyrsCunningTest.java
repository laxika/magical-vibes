package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SatyrsCunning.class, NyxbornColossus.class})
class SatyrsCunningTest extends BaseCardTest {

    @Test
    void castingCreatesASatyrToken() {
        harness.setHand(player1, List.of(new SatyrsCunning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = findToken(player1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SATYR);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void escapedSpellExilesTwoOtherCardsAndCreatesAToken() {
        SatyrsCunning cunning = new SatyrsCunning();
        NyxbornColossus first = new NyxbornColossus();
        NyxbornColossus second = new NyxbornColossus();
        harness.setGraveyard(player1, List.of(cunning, first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        harness.passBothPriorities();

        assertThat(findToken(player1)).isNotNull();
    }

    @Test
    void escapeRequiresTwoOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new SatyrsCunning(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createdSatyrCannotBlock() {
        harness.setHand(player1, List.of(new SatyrsCunning()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new NyxbornColossus());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findToken(player1));
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(tokenIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    @Test
    void escapedSorceryReturnsToGraveyardAndCanEscapeAgain() {
        SatyrsCunning cunning = new SatyrsCunning();
        harness.setGraveyard(player1, List.of(cunning, new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cunning);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(cunning);
        int index = gd.playerGraveyards.get(player1.getId()).indexOf(cunning);
        List<Integer> otherIndices = java.util.stream.IntStream.range(0, 3)
                .filter(i -> i != index).boxed().toList();
        harness.castFromGraveyard(player1, index, otherIndices);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cunning);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4);
    }

    @Test
    void escapeCannotExileItselfOrTheSameCardTwice() {
        SatyrsCunning cunning = new SatyrsCunning();
        NyxbornColossus first = new NyxbornColossus();
        NyxbornColossus second = new NyxbornColossus();
        harness.setGraveyard(player1, List.of(cunning, first, second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cunning, first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void escapeRequiresItsAlternateManaCost() {
        harness.setGraveyard(player1, List.of(new SatyrsCunning(),
                new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
