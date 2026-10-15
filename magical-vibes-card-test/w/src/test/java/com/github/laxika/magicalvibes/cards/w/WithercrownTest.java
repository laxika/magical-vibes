package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Withercrown.class, AirElemental.class, GrizzlyBears.class, DressDown.class})
class WithercrownTest extends BaseCardTest {

    @Test
    void setsOnlyEnchantedCreatureBasePowerToZero() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        attachWithercrown(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void decliningToSacrificeEnchantedCreatureLosesOneLife() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void sacrificingEnchantedCreatureDoesNotSacrificeAnotherCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
    }

    @Test
    void auraControllerDoesNotGetEnchantedCreatureTrigger() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void laterAbilityRemovalPreventsGrantedUpkeepTrigger() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        harness.addToBattlefield(player1, new DressDown());
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void countersStillModifyPowerAndToughness() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attachWithercrown(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void sacrificingCreatureAvoidsLifeLossAndPutsAuraInGraveyard() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Withercrown");
    }

    @Test
    void removingAuraAfterTriggerStillAllowsSacrificingCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        Permanent aura = findPermanent(player1, "Withercrown");
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureLeavingAfterTriggerDoesNotAvoidLifeLoss() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        attachWithercrown(creature);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherCreature);
    }

    @Test
    void resolvingAuraChangesOnlyTargetCreaturesPower() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Withercrown()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Withercrown");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    private void attachWithercrown(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Withercrown());
        aura.setAttachedTo(creature.getId());
    }
}
