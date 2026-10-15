package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HenzieToolboxTorre;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({CaldaiaGuardian.class, AirElemental.class, GrizzlyBears.class, NantukoHusk.class,
        HenzieToolboxTorre.class, WrathOfGod.class})
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
        resolveAllTriggers();

        Permanent guardian = findPermanent(player1, "Caldaia Guardian");
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Caldaia Guardian");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }

    @Test
    void blitzHasHasteImmediatelyWhenSpellResolves() {
        harness.setHand(player1, List.of(new CaldaiaGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Caldaia Guardian"), Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void henzieReducesGuardiansPrintedBlitzCost() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.setHand(player1, List.of(new CaldaiaGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Caldaia Guardian")).hasSize(1);
    }

    @Test
    void createsCitizensForAnotherGuardianWithManaValueExactlyFour() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new NantukoHusk());
        Permanent dyingGuardian = findPermanents(player1, "Caldaia Guardian").get(1);

        harness.activateAbility(player1, 2, null, null);
        harness.handlePermanentChosen(player1, dyingGuardian.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(4);
    }

    @Test
    void doesNotTriggerForOpponentsQualifyingCreature() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new NantukoHusk());
        Permanent elemental = findPermanent(player2, "Air Elemental");

        harness.activateAbility(player2, 1, null, null);
        harness.handlePermanentChosen(player2, elemental.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(findPermanents(player2, "Citizen")).isEmpty();
    }

    @Test
    void normalCastDoesNotGrantHasteSacrificeOrDeathDraw() {
        harness.addToBattlefield(player1, new NantukoHusk());
        harness.setHand(player1, List.of(new CaldaiaGuardian()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Caldaia Guardian"), Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        Permanent guardian = findPermanent(player1, "Caldaia Guardian");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, guardian.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
    }

    @Test
    void observesQualifyingAlliesDyingSimultaneouslyWithIt() {
        harness.addToBattlefield(player1, new CaldaiaGuardian());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Citizen")).hasSize(4);
        assertThat(findPermanents(player2, "Citizen")).isEmpty();
        harness.assertInGraveyard(player1, "Caldaia Guardian");
    }
}
