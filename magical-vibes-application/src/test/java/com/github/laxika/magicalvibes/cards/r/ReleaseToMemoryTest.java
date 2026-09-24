package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReleaseToMemory.class, GrizzlyBears.class, Shock.class})
class ReleaseToMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the opponent's creature cards and creates a colorless Spirit for each")
    void exilesCreaturesAndCreatesSpirits() {
        Card creatureOne = new GrizzlyBears();
        Card creatureTwo = new GrizzlyBears();
        Card nonCreature = new Shock();
        harness.setGraveyard(player2, List.of(creatureOne, nonCreature, creatureTwo));
        harness.setHand(player1, List.of(new ReleaseToMemory()));
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creatureOne.getId(), creatureTwo.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonCreature);

        List<Permanent> tokens = findPermanents(player1, "Spirit");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Creates no Spirits when the opponent's graveyard has no creatures")
    void createsNoSpiritsWithoutCreatures() {
        Card nonCreature = new Shock();
        harness.setGraveyard(player2, List.of(nonCreature));
        harness.setHand(player1, List.of(new ReleaseToMemory()));
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nonCreature);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ReleaseToMemory()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
