package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinFireslinger;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConspicuousSnoopTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast a Goblin spell from the top of the library")
    void castsGoblinFromLibraryTop() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card goblin = new RagingGoblin();
        harness.setLibrary(player1, new ArrayList<>(List.of(goblin)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblin");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(goblin);
    }

    @Test
    @DisplayName("Cannot cast a non-Goblin spell from the top of the library")
    void cannotCastNonGoblinFromLibraryTop() {
        harness.addToBattlefield(player1, new ConspicuousSnoop());
        Card shock = new Shock();
        harness.setLibrary(player1, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
    }

    @Test
    @DisplayName("Gains and can activate the top Goblin's activated abilities")
    void activatesAbilityFromGoblinOnTop() {
        var snoop = harness.addToBattlefieldAndReturn(player1, new ConspicuousSnoop());
        snoop.setSummoningSick(false);
        harness.setLibrary(player1, new ArrayList<>(List.of(new GoblinFireslinger())));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
}
