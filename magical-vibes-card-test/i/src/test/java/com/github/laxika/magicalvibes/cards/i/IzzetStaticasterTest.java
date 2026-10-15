package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IzzetStaticaster.class, DrudgeBeetle.class, AxebaneGuardian.class, WitnessProtection.class})
class IzzetStaticasterTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target creature")
    void damagesTargetCreature() {
        Permanent staticaster = addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(staticaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damages all creatures with the same name across both battlefields")
    void damagesAllCreaturesWithSameName() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent ownBeetle = addCreatureReady(player1, new DrudgeBeetle());
        Permanent opponentBeetle1 = addCreatureReady(player2, new DrudgeBeetle());
        Permanent opponentBeetle2 = addCreatureReady(player2, new DrudgeBeetle());
        Permanent guardian = addCreatureReady(player2, new AxebaneGuardian());

        harness.activateAbility(player1, 0, null, opponentBeetle1.getId());
        harness.passBothPriorities();

        assertThat(ownBeetle.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentBeetle1.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentBeetle2.getMarkedDamage()).isEqualTo(1);
        assertThat(guardian.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Same-name hexproof creature is still damaged (not targeted)")
    void damagesSameNameHexproofCreature() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent hexproof = addCreatureReady(player2, new DrudgeBeetle());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(hexproof.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a hexproof creature directly")
    void cannotTargetHexproofCreature() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent hexproof = addCreatureReady(player2, new DrudgeBeetle());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hexproof.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent other = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Haste lets the tap ability activate the turn it enters")
    void hasteAllowsActivationSameTurn() {
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());

        harness.castFromHand(player1, new IzzetStaticaster(), "{1}{U}{R}");
        harness.passBothPriorities();

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Izzet Staticaster"));
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new IzzetStaticaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new IzzetStaticaster(), "{1}{U}{R}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Izzet Staticaster")).isEqualTo(1);
    }

    @Test
    void abilityStillResolvesAfterSourceLeaves() {
        Permanent source = addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent other = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(other.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void abilityDoesNotResolveWhenTargetGainsHexproof() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent other = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, null, target.getId());
        TestCards.mutableCard(target).setKeywords(EnumSet.of(Keyword.HEXPROOF));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    void faceDownTargetDoesNotShareItsPrintedName() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent faceUp = addCreatureReady(player2, new DrudgeBeetle());
        Permanent faceDown = addCreatureReady(player2, new DrudgeBeetle());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(faceUp.getMarkedDamage()).isZero();
        assertThat(faceDown.getMarkedDamage()).isZero();
    }

    @Test
    void faceDownCreatureDoesNotShareFaceUpTargetsName() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent faceDown = addCreatureReady(player2, new DrudgeBeetle());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(faceDown.getMarkedDamage()).isZero();
    }

    @Test
    void matchesCurrentNamesAfterWitnessProtection() {
        addCreatureReady(player1, new IzzetStaticaster());
        Permanent target = addCreatureReady(player2, new DrudgeBeetle());
        Permanent renamedGuardian = addCreatureReady(player2, new AxebaneGuardian());
        Permanent unchangedBeetle = addCreatureReady(player2, new DrudgeBeetle());
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        firstAura.setAttachedTo(target.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        secondAura.setAttachedTo(renamedGuardian.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(target, renamedGuardian)
                .contains(unchangedBeetle);
        assertThat(unchangedBeetle.getMarkedDamage()).isZero();
    }

}
