package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gigantoplasm.class, GrizzlyBears.class})
class GigantoplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Gigantoplasm copies a creature and can set its base power and toughness")
    void copiesCreatureAndSetsBasePowerToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent gigantoplasm = findPermanent(player1, "Grizzly Bears");
        assertThat(gigantoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, gigantoplasm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gigantoplasm)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(gigantoplasm), 5, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gigantoplasm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gigantoplasm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Gigantoplasm dies as a 0/0 when it does not copy a creature")
    void diesWhenItDoesNotCopy() {
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gigantoplasm");
        harness.assertInGraveyard(player1, "Gigantoplasm");
    }

    @Test
    void mayDeclineToCopyAnAvailableCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Gigantoplasm");
        harness.assertInGraveyard(player1, "Gigantoplasm");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void basePowerAndToughnessPersistIntoTheNextTurn() {
        Permanent copy = copyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    void mostRecentlyResolvedActivationDeterminesBasePowerAndToughness() {
        Permanent copy = copyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 5, null);
        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(5);
    }

    @Test
    void choosingZeroMakesTheCopyDie() {
        copyBears();
        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Gigantoplasm");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void copyingAnotherGigantoplasmCopiesItsAbilityButNotItsActivatedSize() {
        Permanent firstCopy = copyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());
        Permanent secondCopy = gd.playerBattlefields.get(player1.getId()).get(1);

        assertThat(gqs.getEffectivePower(gd, secondCopy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCopy)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 1, 1, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, secondCopy)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCopy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, firstCopy)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, firstCopy)).isEqualTo(5);
    }

    private Permanent copyBears() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Gigantoplasm(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        return findPermanent(player1, "Grizzly Bears");
    }
}
