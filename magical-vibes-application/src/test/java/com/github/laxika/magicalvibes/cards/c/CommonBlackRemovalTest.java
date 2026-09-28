package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommonBlackRemoval.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class CommonBlackRemovalTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature and creates a Food token")
    void createsFoodToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Destroys a creature and creates a Treasure token")
    void createsTreasureToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Destroys a creature and puts a menace counter on a creature you control")
    void putsMenaceCounterOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target, 2);

        assertThat(ownCreature.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Destroys a creature and its controller mills cards equal to its power")
    void millsDestroyedCreaturesControllerByPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        cast(target, 3);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new CommonBlackRemoval()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target, int mode) {
        harness.setHand(player1, List.of(new CommonBlackRemoval()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
