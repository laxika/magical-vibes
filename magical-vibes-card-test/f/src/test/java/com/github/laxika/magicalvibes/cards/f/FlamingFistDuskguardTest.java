package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvenSunstriker;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingFistDuskguard.class, GrizzlyBears.class, SteadfastPaladin.class, Cancel.class, AvenSunstriker.class})
class FlamingFistDuskguardTest extends BaseCardTest {

    @Test
    @DisplayName("The next creature spell perpetually gets +1/+0")
    void perpetuallyBoostsTheNextCreatureSpell() {
        FlamingFistDuskguard duskguard = new FlamingFistDuskguard();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(duskguard, bears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bearPermanent)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the next creature spell is boosted")
    void onlyBoostsOneCreatureSpell() {
        FlamingFistDuskguard duskguard = new FlamingFistDuskguard();
        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(duskguard, firstBears, secondBears));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent firstPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(firstBears.getId()))
                .findFirst()
                .orElseThrow();
        Permanent secondPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(secondBears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, firstPermanent)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondPermanent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boon stays after Flaming Fist Duskguard leaves the battlefield")
    void boonSurvivesItsSourceLeavingTheBattlefield() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        source.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Flaming Fist Duskguard");

        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Steadfast Paladin");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature spell doesn't use the boon, which lasts through later turns")
    void opponentsCreatureDoesNotConsumeBoonAndBoonSurvivesTurnChanges() {
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SteadfastPaladin()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Steadfast Paladin"))).isEqualTo(2);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Steadfast Paladin"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Several boons all boost the same next creature spell")
    void multipleBoonsAllBoostTheSameNextCreatureSpell() {
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Steadfast Paladin");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The perpetual boost stays on the card after it returns to hand and is cast again")
    void perpetualBoostSurvivesReturningToHandAndBeingCastAgain() {
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent bears = findPermanent(player1, "Steadfast Paladin");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.assertInHand(player1, "Steadfast Paladin");
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent returnedBears = findPermanent(player1, "Steadfast Paladin");
        assertThat(gqs.getEffectivePower(gd, returnedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature spell countered before the boon's ability resolves is not boosted (CR 400.7)")
    void counteredCreatureSpellIsNotBoosted() {
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("A creature spell cast face down is boosted, and the log doesn't name its card (CR 708.5)")
    void faceDownCreatureSpellIsBoostedWithoutNamingIt() {
        harness.enterBattlefieldAndReturn(player1, new FlamingFistDuskguard());
        resolveAllTriggers();
        AvenSunstriker aven = new AvenSunstriker();
        harness.setHand(player1, List.of(aven));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.perpetualCardPowerToughnessModifiers).containsKey(aven.getId());
        assertThat(gameLogContains("A face-down spell perpetually gets +1/+0.")).isTrue();
        assertThat(gameLogContains("Aven Sunstriker")).isFalse();
    }
}
