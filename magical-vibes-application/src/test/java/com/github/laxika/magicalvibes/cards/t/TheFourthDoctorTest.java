package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFourthDoctor.class, AncientDen.class, Forest.class, GrizzlyBears.class, MindStone.class})
class TheFourthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a historic spell from the top creates a Food token")
    void castsHistoricSpellFromTopAndCreatesFood() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        MindStone mindStone = new MindStone();
        harness.setLibrary(player1, List.of(mindStone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Playing a historic land from the top creates a Food token")
    void playsHistoricLandFromTopAndCreatesFood() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        AncientDen ancientDen = new AncientDen();
        harness.setLibrary(player1, List.of(ancientDen));

        harness.castFromLibraryTop(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ancient Den");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("The top-library land or spell permission is shared and limited to once each turn")
    void allowsOnlyOneHistoricTopLibraryPlayEachTurn() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        AncientDen ancientDen = new AncientDen();
        MindStone mindStone = new MindStone();
        harness.setLibrary(player1, List.of(ancientDen, mindStone));

        harness.castFromLibraryTop(player1);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mindStone);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Nonhistoric cards cannot be played from the top through The Fourth Doctor")
    void rejectsNonhistoricTopCard() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Nonhistoric spells cannot be cast from the top through The Fourth Doctor")
    void rejectsNonhistoricTopSpell() {
        harness.addToBattlefield(player1, new TheFourthDoctor());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }
}
