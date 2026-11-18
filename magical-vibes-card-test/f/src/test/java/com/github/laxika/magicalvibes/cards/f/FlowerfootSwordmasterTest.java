package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.b.BloodstoneGoblin;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowerfootSwordmaster.class, GiantGrowth.class, GrizzlyBears.class, BraveKinDuo.class,
        BloodstoneGoblin.class})
class FlowerfootSwordmasterTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new FlowerfootSwordmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(1);
    }

    @Test
    void valiantBoostsMiceOnlyOnceWhenTargetedByYourSpellsEachTurn() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();
        int powerAfterFirstSpell = gqs.getEffectivePower(gd, swordmaster);

        assertThat(powerAfterFirstSpell).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(powerAfterFirstSpell + 3);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, swordmaster.getId());

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(4);
    }

    @Test
    void offspringDoesNotCreateTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new FlowerfootSwordmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new BloodstoneGoblin());
        harness.setHand(player1, List.of(new FlowerfootSwordmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    void valiantTriggersBeforeYourActivatedAbilityAndBoostsOnlyYourMice() {
        Permanent duo = addCreatureReady(player1, new BraveKinDuo());
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        Permanent opposingMouse = harness.addToBattlefieldAndReturn(player2, new FlowerfootSwordmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swordmaster)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, duo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, duo)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingMouse)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swordmaster)).isEqualTo(3);
        Permanent lateMouse = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        assertThat(gqs.getEffectivePower(gd, lateMouse)).isEqualTo(1);
    }

    @Test
    void opponentsTargetingDoesNotUseUpValiantForYourSpell() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, swordmaster.getId());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, swordmaster)).isEqualTo(8);
    }

    @Test
    void offspringTokenHasItsOwnValiantTriggerWithoutCreatingMoreOffspring() {
        harness.setHand(player1, List.of(new FlowerfootSwordmaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken()).findFirst().orElseThrow();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    void valiantBoostExpiresAndCanTriggerAgainOnTheOpponentsTurn() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swordmaster)).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swordmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, swordmaster)).isEqualTo(5);
    }
}
