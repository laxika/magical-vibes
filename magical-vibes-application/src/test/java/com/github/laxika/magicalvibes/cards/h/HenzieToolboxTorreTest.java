package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HenzieToolboxTorre.class, AirElemental.class, GrizzlyBears.class})
class HenzieToolboxTorreTest extends BaseCardTest {

    @Test
    @DisplayName("Grants blitz to creature spells with mana value 4 or greater")
    void grantsBlitzToLargeCreatureSpells() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Commander casts reduce the generic portion of blitz costs")
    void commanderCastCountReducesBlitzCost() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Air Elemental").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant blitz to creature spells with mana value less than 4")
    void doesNotGrantBlitzToSmallCreatureSpells() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
