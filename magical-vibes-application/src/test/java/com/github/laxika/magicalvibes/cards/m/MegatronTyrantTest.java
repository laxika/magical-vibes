package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BasiliskCollar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MegatronTyrant.class, MegatronDestructiveForce.class, GrizzlyBears.class,
        MonssGoblinRaiders.class,
        Shock.class, WornPowerstone.class, BasiliskCollar.class, Unsummon.class})
class MegatronTyrantTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsMegatronConvertedWithLivingMetal() {
        Permanent megatron = castMegatronConverted();

        assertThat(megatron.isTransformed()).isTrue();
        assertThat(megatron.getCard()).isInstanceOf(MegatronDestructiveForce.class);
        assertThat(gqs.isCreature(gd, megatron)).isTrue();
    }

    @Test
    void postcombatMainMayConvertsAndAddsColorlessManaForOpponentsLifeLost() {
        Permanent megatron = harness.addToBattlefieldAndReturn(player1, new MegatronTyrant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.lifeLostThisTurn.get(player2.getId())).isEqualTo(2);
        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(megatron.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void attackMaySacrificeArtifactToDamageTargetCreature() {
        Permanent megatron = castMegatronConverted();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MonssGoblinRaiders());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(megatron)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(findPermanent(player1, "Megatron, Tyrant").isTransformed()).isFalse();
    }

    @Test
    void controllerCanCastDuringCombat() {
        harness.addToBattlefield(player1, new MegatronTyrant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentCannotCastDuringCombat() {
        harness.addToBattlefield(player1, new MegatronTyrant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
    }

    @Test
    void livingMetalDoesNotMakeMegatronACreatureDuringOpponentsTurn() {
        Permanent megatron = castMegatronConverted();
        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, megatron)).isFalse();
    }

    @Test
    void decliningPostcombatConversionAddsNoMana() {
        Permanent megatron = harness.addToBattlefieldAndReturn(player1, new MegatronTyrant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        gd.playerManaPools.get(player1.getId()).clear();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(megatron.isTransformed()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void leavingBeforePostcombatTriggerResolvesDoesNotAwardMana() {
        Permanent megatron = harness.addToBattlefieldAndReturn(player1, new MegatronTyrant());
        harness.setHand(player1, List.of(new Shock(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        advanceToPostcombatMain();
        harness.castAndResolveInstant(player1, 0, megatron.getId());
        harness.assertInHand(player1, "Megatron, Tyrant");
        gd.playerManaPools.get(player1.getId()).clear();
        if (!(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice)) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void deathtouchMakesOneDamageLethalForDeterminingExcess() {
        Permanent megatron = castMegatronConverted();
        Permanent collar = harness.addToBattlefieldAndReturn(player1, new BasiliskCollar());
        collar.setAttachedTo(megatron.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(megatron)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        assertThat(megatron.isTransformed()).isFalse();
    }

    @Test
    void damageWithoutExcessDoesNotConvertOrDamageController() {
        Permanent megatron = castMegatronConverted();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(megatron)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.handlePermanentChosen(player1, megatron.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, harness::passBothPriorities);

        assertThat(megatron.getMarkedDamage()).isEqualTo(3);
        assertThat(megatron.isTransformed()).isTrue();
        harness.assertLife(player1, 20);
    }

    @Test
    void decliningAttackSacrificeKeepsArtifactAndConvertedFace() {
        Permanent megatron = castMegatronConverted();
        harness.addToBattlefield(player1, new WornPowerstone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(megatron)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Worn Powerstone");
        assertThat(megatron.isTransformed()).isTrue();
        harness.assertLife(player2, 20);
    }

    private Permanent castMegatronConverted() {
        harness.setHand(player1, List.of(new MegatronTyrant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent megatron = findPermanent(player1, "Megatron, Destructive Force");
        megatron.setSummoningSick(false);
        return megatron;
    }

    private void advanceToPostcombatMain() {
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
    }
}
