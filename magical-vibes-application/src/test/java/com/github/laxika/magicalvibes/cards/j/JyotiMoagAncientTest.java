package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.s.SatyrWayfinder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({JyotiMoagAncient.class, SatyrWayfinder.class})
class JyotiMoagAncientTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Forest Dryad land creature for each commander cast")
    void createsForestDryadsForCommanderCasts() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player1.getId());

        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();

        List<Permanent> dryads = findPermanents(player1, "Forest Dryad");
        assertThat(dryads).hasSize(2);
        assertThat(dryads).allSatisfy(dryad -> {
            assertThat(dryad.getCard().hasType(CardType.LAND)).isTrue();
            assertThat(dryad.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(dryad.getCard().getSubtypes())
                    .contains(CardSubtype.FOREST, CardSubtype.DRYAD);
            assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
            assertThat(dryad.isSummoningSick()).isTrue();
            assertThat(dryad.isTapped()).isFalse();
        });
    }

    @Test
    @DisplayName("Gives land creatures +2/+2 at the beginning of combat")
    void boostsLandCreaturesByJyotisPower() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        Permanent nonlandCreature = harness.addToBattlefieldAndReturn(player1, new SatyrWayfinder());
        resolveAllTriggers();
        Permanent dryad = findPermanent(player1, "Forest Dryad");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonlandCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonlandCreature)).isEqualTo(1);
    }

    @Test
    void createsNoTokensWithoutOwnCommanderCasts() {
        gd.recordCommanderCastFromCommandZone(player2.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest Dryad")).isEmpty();
        assertThat(findPermanents(player2, "Forest Dryad")).isEmpty();
    }

    @Test
    void countsCommanderCastsWhenEntryAbilityResolves() {
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        gd.recordCommanderCastFromCommandZone(player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Forest Dryad")).hasSize(1);
    }

    @Test
    void boostsOnlyItsControllersLandCreaturesDuringOpponentCombat() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        gd.recordCommanderCastFromCommandZone(player2.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();
        Permanent opposingJyoti = harness.enterBattlefieldAndReturn(player2, new JyotiMoagAncient());
        resolveAllTriggers();
        opposingJyoti.setPowerModifier(3);
        Permanent ownDryad = findPermanent(player1, "Forest Dryad");
        Permanent opposingDryad = findPermanent(player2, "Forest Dryad");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ownDryad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownDryad)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingDryad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opposingDryad)).isEqualTo(6);
    }

    @Test
    void usesPowerAtResolutionAndLocksInTheBonus() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jyoti = harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();
        Permanent dryad = findPermanent(player1, "Forest Dryad");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        jyoti.setPowerModifier(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(6);
        jyoti.setPowerModifier(0);
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(6);
    }

    @Test
    void negativeSourcePowerGivesZeroBonus() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        Permanent jyoti = harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();
        jyoti.setPowerModifier(-3);
        Permanent dryad = findPermanent(player1, "Forest Dryad");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    void additionalCombatBonusesAccumulateAndExpireAtEndOfTurn() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();
        Permanent dryad = findPermanent(player1, "Forest Dryad");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(3);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(5);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, dryad)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dryad)).isEqualTo(1);
    }

    @Test
    void forestDryadCannotTapForManaUntilSummoningSicknessEnds() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new JyotiMoagAncient());
        resolveAllTriggers();
        Permanent dryad = findPermanent(player1, "Forest Dryad");
        int dryadIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dryad);

        assertThatThrownBy(() -> harness.tapPermanent(player1, dryadIndex))
                .isInstanceOf(IllegalStateException.class);
        assertThat(dryad.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.performUntapStep(player1);
        harness.tapPermanent(player1, dryadIndex);
        assertThat(dryad.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
