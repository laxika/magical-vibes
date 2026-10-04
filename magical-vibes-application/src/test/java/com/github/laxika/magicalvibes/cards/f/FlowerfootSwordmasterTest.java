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
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void valiantBoostsMiceOnlyOnceWhenTargetedByYourSpellsEachTurn() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();
        int powerAfterFirstSpell = swordmaster.getEffectivePower();

        assertThat(powerAfterFirstSpell).isEqualTo(5);
        assertThat(bear.getEffectivePower()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(powerAfterFirstSpell + 3);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, swordmaster.getId());

        assertThat(swordmaster.getEffectivePower()).isEqualTo(4);
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

        assertThat(goblin.getEffectivePower()).isEqualTo(2);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void valiantTriggersBeforeYourActivatedAbilityAndBoostsOnlyYourMice() {
        Permanent duo = addCreatureReady(player1, new BraveKinDuo());
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        Permanent opposingMouse = harness.addToBattlefieldAndReturn(player2, new FlowerfootSwordmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(2);
        assertThat(swordmaster.getEffectiveToughness()).isEqualTo(2);
        assertThat(duo.getEffectivePower()).isEqualTo(2);
        assertThat(duo.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingMouse.getEffectivePower()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(3);
        assertThat(swordmaster.getEffectiveToughness()).isEqualTo(3);
        Permanent lateMouse = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        assertThat(lateMouse.getEffectivePower()).isEqualTo(1);
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

        assertThat(swordmaster.getEffectivePower()).isEqualTo(8);
        assertThat(swordmaster.getEffectiveToughness()).isEqualTo(8);
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
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(original.getEffectivePower()).isEqualTo(6);
        assertThat(token.getEffectivePower()).isEqualTo(6);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void valiantBoostExpiresAndCanTriggerAgainOnTheOpponentsTurn() {
        Permanent swordmaster = harness.addToBattlefieldAndReturn(player1, new FlowerfootSwordmaster());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(swordmaster.getEffectivePower()).isEqualTo(1);
        assertThat(swordmaster.getEffectiveToughness()).isEqualTo(2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, swordmaster.getId());
        harness.passBothPriorities();

        assertThat(swordmaster.getEffectivePower()).isEqualTo(5);
        assertThat(swordmaster.getEffectiveToughness()).isEqualTo(5);
    }
}
