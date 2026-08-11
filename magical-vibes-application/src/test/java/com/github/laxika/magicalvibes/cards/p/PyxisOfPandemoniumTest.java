package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PyxisOfPandemoniumTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability exiles the top card of each library face down")
    void tapAbilityExilesTopCardOfEachLibraryFaceDown() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .filteredOn(entry -> pyxis.getId().equals(entry.sourcePermanentId()))
                .allSatisfy(entry -> assertThat(entry.faceDown()).isTrue());
    }

    @Test
    @DisplayName("Sacrifice ability reveals the pile and returns only permanent cards")
    void sacrificeAbilityRevealsPileAndReturnsPermanentCards() {
        Permanent pyxis = harness.addToBattlefieldAndReturn(player1, new PyxisOfPandemonium());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        pyxis.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyxis of Pandemonium");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards)
                .filteredOn(entry -> pyxis.getId().equals(entry.sourcePermanentId()))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.card().getName()).isEqualTo("Shock");
                    assertThat(entry.faceDown()).isFalse();
                });
    }
}
