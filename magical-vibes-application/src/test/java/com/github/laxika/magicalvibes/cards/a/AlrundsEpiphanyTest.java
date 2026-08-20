package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlrundsEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two flying Bird tokens, takes an extra turn, and is exiled")
    void resolvesItsEffects() {
        AlrundsEpiphany epiphany = new AlrundsEpiphany();
        harness.setHand(player1, List.of(epiphany));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> birds = findPermanents(player1, "Bird");
        assertThat(birds).hasSize(2);
        assertThat(birds).allSatisfy(bird -> {
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
            assertThat(bird.getEffectivePower()).isEqualTo(1);
            assertThat(bird.getEffectiveToughness()).isEqualTo(1);
            assertThat(bird.hasKeyword(Keyword.FLYING)).isTrue();
        });
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(epiphany.getId()));
        harness.assertNotInGraveyard(player1, "Alrund's Epiphany");
    }

    @Test
    @DisplayName("Can be foretold and cast for its foretell cost on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        AlrundsEpiphany epiphany = new AlrundsEpiphany();
        harness.setHand(player1, List.of(epiphany));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(epiphany.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, epiphany.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(epiphany.getId()));
    }
}
