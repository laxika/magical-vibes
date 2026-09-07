package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenReedstalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErrantAndGiada.class, AvenReedstalker.class, GrizzlyBears.class, StormCrow.class})
class ErrantAndGiadaTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a flying creature spell from the top of the library")
    void castsFlyingCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card stormCrow = new StormCrow();
        gd.playerDecks.get(player1.getId()).addFirst(stormCrow);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Storm Crow");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(stormCrow);
    }

    @Test
    @DisplayName("Can cast a flash creature spell from the top of the library")
    void castsFlashCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card avenReedstalker = new AvenReedstalker();
        gd.playerDecks.get(player1.getId()).addFirst(avenReedstalker);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Aven Reedstalker");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(avenReedstalker);
    }

    @Test
    @DisplayName("Cannot cast a creature without flying or flash from the top of the library")
    void cannotCastCreatureWithoutFlyingOrFlashFromLibraryTop() {
        harness.addToBattlefield(player1, new ErrantAndGiada());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }
}
