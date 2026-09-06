package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheFuture.class, Forest.class, GrizzlyBears.class, Opt.class})
class MagusOfTheFutureTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a land from the top of their library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may cast a creature spell from the top of their library")
    void castsCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("The controller may cast an instant spell from the top of their library")
    void castsInstantFromLibraryTop() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(opt);
    }

    @Test
    @DisplayName("The top-library permissions apply only to the Magus's controller")
    void permissionsOnlyApplyToController() {
        harness.addToBattlefield(player1, new MagusOfTheFuture());
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(forest));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(forest);
    }
}
