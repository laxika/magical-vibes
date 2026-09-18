package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalSkullIsuSpyglass.class, AncientDen.class, Forest.class, GrizzlyBears.class, MindStone.class})
class CrystalSkullIsuSpyglassTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may play a historic land from the top of their library")
    void playsHistoricLandFromLibraryTop() {
        harness.addToBattlefield(player1, new CrystalSkullIsuSpyglass());
        AncientDen land = new AncientDen();
        harness.setLibrary(player1, List.of(land));

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Ancient Den");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("The controller cannot play a nonhistoric land from the top of their library")
    void cannotPlayNonhistoricLandFromLibraryTop() {
        harness.addToBattlefield(player1, new CrystalSkullIsuSpyglass());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The controller may cast a historic spell from the top of their library")
    void castsHistoricSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new CrystalSkullIsuSpyglass());
        MindStone mindStone = new MindStone();
        harness.setLibrary(player1, List.of(mindStone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(mindStone);
    }

    @Test
    @DisplayName("The controller cannot cast a nonhistoric spell from the top of their library")
    void cannotCastNonhistoricSpellFromLibraryTop() {
        harness.addToBattlefield(player1, new CrystalSkullIsuSpyglass());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Tapping Crystal Skull adds blue mana")
    void tapsForBlueMana() {
        Permanent spyglass = harness.addToBattlefieldAndReturn(player1, new CrystalSkullIsuSpyglass());

        harness.activateAbility(player1, 0, null, null);

        assertThat(spyglass.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
