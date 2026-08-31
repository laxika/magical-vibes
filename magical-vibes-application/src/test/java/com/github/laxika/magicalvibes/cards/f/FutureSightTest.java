package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FutureSight.class, GrizzlyBears.class})
class FutureSightTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new FutureSight());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("The controller may cast a spell from the top of their library")
    void castsSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new FutureSight());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }
}
