package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowansTalent.class, JaceBeleren.class, ChandraNovicePyromancer.class,
        GrizzlyBears.class, AirElemental.class})
class RowansTalentTest extends BaseCardTest {

    @Test
    void grantedLoyaltyAbilityBoostsCreatureAndCopyCanRetarget() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace),
                grantedAbilityIndex,
                null,
                firstBear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondBear.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.TRAMPLE)).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void copiesOnlyTheEnchantedPlaneswalkersLoyaltyAbility() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent chandra = addReadyPlaneswalker(player1, new ChandraNovicePyromancer(), 4);
        attachTalent(jace);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace), 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        Permanent elemental = addCreatureReady(player1, new AirElemental());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(6);
    }

    @Test
    void grantedAbilityCanChooseNoTargetsEvenWhenACreatureIsAvailable() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace),
                grantedAbilityIndex, null, null);
        resolveAllTriggers();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void keepingTheOriginalTargetStillCreatesAMandatoryCopyAndBonusesExpire() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace),
                grantedAbilityIndex, null, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opponentCanUseGrantedAbilityButTheirActivationIsNotCopied() {
        Permanent jace = addReadyPlaneswalker(player2, new JaceBeleren(), 4);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(jace),
                grantedAbilityIndex, null, bear.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAndPrintedAbilitiesShareTheLoyaltyActivationLimit() {
        Permanent jace = addReadyPlaneswalker(player1, new JaceBeleren(), 4);
        attachTalent(jace);

        int grantedAbilityIndex = gs.getEffectiveActivatedAbilities(gd, jace).size() - 1;
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace),
                grantedAbilityIndex, null, null);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(jace), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void manaProducingLoyaltyAbilityIsCopiedWithoutPayingItsCostAgain() {
        Permanent chandra = addReadyPlaneswalker(player1, new ChandraNovicePyromancer(), 4);
        attachTalent(chandra);

        int manaBefore = gd.playerManaPools.get(player1.getId()).getTotal();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(chandra), 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(manaBefore + 4);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void auraCanBeCastOnAnOpponentPlaneswalker() {
        Permanent jace = addReadyPlaneswalker(player2, new JaceBeleren(), 4);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new RowansTalent()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0, jace.getId());
        harness.passBothPriorities();

        Permanent talent = findPermanent(player1, "Rowan's Talent");
        assertThat(talent.getAttachedTo()).isEqualTo(jace.getId());
    }

    @Test
    void auraCannotTargetANonPlaneswalkerCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RowansTalent()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void attachTalent(Permanent planeswalker) {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new RowansTalent());
        talent.setAttachedTo(planeswalker.getId());
    }

    private Permanent addReadyPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return planeswalker;
    }
}
