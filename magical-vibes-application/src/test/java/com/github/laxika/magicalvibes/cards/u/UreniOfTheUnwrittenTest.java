package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UreniOfTheUnwritten.class, ShivanDragon.class, GrizzlyBears.class, Forest.class})
class UreniOfTheUnwrittenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a Dragon creature from the top eight cards")
    void etbOffersDragonFromTopEight() {
        ShivanDragon dragon = new ShivanDragon();
        setLibrary(new GrizzlyBears(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), dragon);

        harness.enterBattlefieldAndReturn(player1, new UreniOfTheUnwritten());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("Attacking triggers the same Dragon placement ability")
    void attackOffersDragonFromTopEight() {
        Permanent ureni = addCreatureReady(player1, new UreniOfTheUnwritten());
        ShivanDragon dragon = new ShivanDragon();
        setLibrary(new Forest(), new GrizzlyBears(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), dragon);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(dragon.getId());
        assertThat(ureni.isAttacking()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(dragon.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(dragon);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
