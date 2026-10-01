package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PearlLakeWarden.class, Forest.class, GrizzlyBears.class})
class PearlLakeWardenTest extends BaseCardTest {

    @Test
    void castsItselfFromTheTopOfItsLibrary() {
        PearlLakeWarden warden = new PearlLakeWarden();
        harness.setLibrary(player1, List.of(warden, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pearl Lake Warden");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(warden);
    }

    @Test
    void cannotCastAnotherCardFromTheTopThroughItsPermission() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, new PearlLakeWarden()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void adventureSeeksALandOntoTheBattlefield() {
        PearlLakeWarden warden = new PearlLakeWarden();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(warden));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(warden.getId())).isNotNull();
    }
}
