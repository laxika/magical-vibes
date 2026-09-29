package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoomsdayConfluence.class, GrizzlyBears.class, Spellbook.class})
class DoomsdayConfluenceTest extends BaseCardTest {

    @Test
    void choosesRepeatedModesAndPaysX() {
        harness.setHand(player1, List.of(new DoomsdayConfluence()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        int modes = ChooseOneEffect.encodeRepeatedModeSelectionInRange(0, Integer.MAX_VALUE, 3, 1, 1, 2);
        gs.playModalXCard(gd, player1, 0, modes, 3, null, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(countPermanents(player1, "Dalek")).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void eachPlayerSacrificesANonartifactCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new DoomsdayConfluence()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int modes = ChooseOneEffect.encodeRepeatedModeSelectionInRange(0, Integer.MAX_VALUE, 3, 0);
        gs.playModalXCard(gd, player1, 0, modes, 1, null, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
    }

    @Test
    void requiresExactlyThePaidXNumberOfModes() {
        harness.setHand(player1, List.of(new DoomsdayConfluence()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        int modes = ChooseOneEffect.encodeRepeatedModeSelectionInRange(0, Integer.MAX_VALUE, 3, 1, 2);
        assertThatThrownBy(() -> gs.playModalXCard(gd, player1, 0, modes, 3, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
