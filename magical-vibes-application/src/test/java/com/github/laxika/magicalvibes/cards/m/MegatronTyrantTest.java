package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MonssGoblinRaiders;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MegatronTyrant.class, MegatronDestructiveForce.class, GrizzlyBears.class,
        MonssGoblinRaiders.class,
        Shock.class, WornPowerstone.class})
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
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
