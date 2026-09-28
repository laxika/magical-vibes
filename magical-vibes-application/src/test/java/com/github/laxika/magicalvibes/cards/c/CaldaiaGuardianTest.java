package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
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

@CardUsed({CaldaiaGuardian.class, AirElemental.class, GrizzlyBears.class, NantukoHusk.class})
class CaldaiaGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two Citizens when a controlled creature with mana value 4 or greater dies")
    void createsCitizensForLargeAllyDeath() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new NantukoHusk());

        Permanent elemental = findPermanent(player1, "Air Elemental");
        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, elemental.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Citizens when a controlled creature with mana value less than 4 dies")
    void doesNotCreateCitizensForSmallAllyDeath() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NantukoHusk());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
    }

    @Test
    @DisplayName("Creates Citizens when Caldaia Guardian itself dies")
    void createsCitizensForItsOwnDeath() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new NantukoHusk());

        Permanent guardian = findPermanent(player1, "Caldaia Guardian");
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, guardian.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, sacrifices at the next end step, and creates Citizens")
    void blitzGrantsHasteDrawsAndCreatesCitizens() {
        harness.setHand(player1, List.of(new CaldaiaGuardian()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent guardian = findPermanent(player1, "Caldaia Guardian");
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Caldaia Guardian");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }
}
