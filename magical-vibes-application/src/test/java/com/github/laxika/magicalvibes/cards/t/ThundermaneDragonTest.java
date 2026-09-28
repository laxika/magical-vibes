package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoreclawTerrorOfQalSisma;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThundermaneDragon.class, GoreclawTerrorOfQalSisma.class, GrizzlyBears.class})
class ThundermaneDragonTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature with power 4 or greater from the top and gives it haste")
    void castsHighPowerCreatureFromLibraryTopWithHaste() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        GoreclawTerrorOfQalSisma goreclaw = new GoreclawTerrorOfQalSisma();
        harness.setLibrary(player1, List.of(goreclaw));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFromLibraryTop(player1);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Goreclaw, Terror of Qal Sisma");
        assertThat(gqs.hasKeyword(gd, entered, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("does not allow a creature with power less than 4 from the top")
    void cannotCastLowPowerCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new ThundermaneDragon());
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }
}
