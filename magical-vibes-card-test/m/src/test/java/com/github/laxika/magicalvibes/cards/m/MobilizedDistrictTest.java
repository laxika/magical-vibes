package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.k.KrenkoTinStreetKingpin;
import com.github.laxika.magicalvibes.cards.s.SparkDouble;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MobilizedDistrict.class, KrenkoTinStreetKingpin.class, GideonBlackblade.class, SparkDouble.class})
class MobilizedDistrictTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Mobilized District produces colorless mana")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new MobilizedDistrict());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mobilized District becomes a vigilant 3/3 Citizen that remains a land")
    void animatesIntoCitizen() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, district)).isTrue();
        assertThat(gqs.getEffectivePower(gd, district)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, district)).isEqualTo(3);
        assertThat(district.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(district.getGrantedKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Legendary creatures and planeswalkers reduce the animation cost")
    void legendaryCreaturesAndPlaneswalkersReduceActivationCost() {
        harness.addToBattlefield(player1, new MobilizedDistrict());
        harness.addToBattlefield(player1, new KrenkoTinStreetKingpin());
        harness.addToBattlefield(player1, new GideonBlackblade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mobilized District's animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, district)).isFalse();
    }

    @Test
    @DisplayName("A planeswalker that is also a legendary creature reduces the cost only once")
    void creaturePlaneswalkerCountsOnlyOnce() {
        harness.addToBattlefield(player1, new MobilizedDistrict());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opposing legendary creatures and planeswalkers do not reduce the cost")
    void opposingPermanentsDoNotReduceCost() {
        harness.addToBattlefield(player1, new MobilizedDistrict());
        harness.addToBattlefield(player2, new KrenkoTinStreetKingpin());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A nonlegendary Spark Double planeswalker does not reduce the cost")
    void nonlegendaryPlaneswalkerDoesNotReduceCost() {
        harness.addToBattlefield(player1, new MobilizedDistrict());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.castFromHand(player1, new SparkDouble(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, gideon.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Animation can be activated while the land is tapped and does not untap it")
    void tappedLandCanAnimate() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, district)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, district, CardSubtype.CITIZEN)).isTrue();
        assertThat(district.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animating again preserves counters and does not end the animation in the end step")
    void repeatedAnimationPreservesCountersThroughEndStep() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        district.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gqs.isCreature(gd, district)).isTrue();
        assertThat(gqs.getEffectivePower(gd, district)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, district)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, district, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, district, CardSubtype.CITIZEN)).isTrue();
    }

    @Test
    @DisplayName("An animated District attacks without tapping thanks to vigilance")
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent district = addCreatureReady(player1, new MobilizedDistrict());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(district.isAttacking()).isTrue();
        assertThat(district.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A newly entered District can animate but cannot tap for mana as a creature")
    void summoningSicknessPreventsAnimatedManaAbility() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new MobilizedDistrict());
        district.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(district.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
