package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealmwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("casts a creature spell of the chosen type from the top of the library")
    void castsCreatureOfChosenTypeFromLibraryTop() {
        Realmwalker realmwalker = new Realmwalker();
        harness.setHand(player1, List.of(realmwalker));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("does not cast a creature of a different type from the top of the library")
    void rejectsCreatureOfDifferentTypeFromLibraryTop() {
        castRealmwalkerChoosingBear();
        Card elf = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(elf);
    }

    private void castRealmwalkerChoosingBear() {
        Realmwalker realmwalker = new Realmwalker();
        harness.setHand(player1, List.of(realmwalker));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
    }
}
