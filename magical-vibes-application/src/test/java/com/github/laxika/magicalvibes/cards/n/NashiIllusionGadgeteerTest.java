package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NashiIllusionGadgeteer.class, GrizzlyBears.class, Forest.class})
class NashiIllusionGadgeteerTest extends BaseCardTest {

    @Test
    void secretlyConjuresAFlashDuplicateOfAChosenNonland() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));

        castNashi();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo("Grizzly Bears");
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(duplicate.hasKeyword(Keyword.FLASH)).isTrue();
        assertThat(duplicate.getId()).isNotEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
    }

    @Test
    void automaticallyConjuresAChosenLandWhenItIsTheOnlyGraveyardCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        castNashi();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate.getName()).isEqualTo("Forest");
        assertThat(duplicate.isTokenCard()).isTrue();
        assertThat(duplicate.hasKeyword(Keyword.FLASH)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
    }

    private void castNashi() {
        harness.setHand(player1, List.of(new NashiIllusionGadgeteer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
