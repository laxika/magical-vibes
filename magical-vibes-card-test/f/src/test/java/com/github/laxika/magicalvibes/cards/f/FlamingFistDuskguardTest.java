package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingFistDuskguard.class, GrizzlyBears.class, SteadfastPaladin.class})
class FlamingFistDuskguardTest extends BaseCardTest {

    @Test
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
}
