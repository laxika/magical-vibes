package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FoeRazerRegent;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GratuitousViolence;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PredatoryUrge.class, HillGiant.class, GiantSpider.class, Forest.class})
class PredatoryUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature taps to deal mutual power damage to a target creature")
    void enchantedCreatureFightsTargetCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(enchantedCreature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(enchantedCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Granted ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(enchantedCreature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void auraCanBeCastToGrantTheAbility() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new PredatoryUrge()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({IntoTheRoil.class})
    void sourceLeavingDoesNotPreventItsDamage() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({IntoTheRoil.class})
    void auraLeavingDoesNotRemoveTheActivatedAbilityFromTheStack() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({IntoTheRoil.class})
    void targetLeavingPreventsBothDamageInstructions() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void creatureCanTargetItselfAndDealDamageTwice() {
        Permanent creature = addCreatureReady(player1, new GiantSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GiantSpider);
    }

    @Test
    void creatureControllerCanActivateAnOpponentsAura() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void summoningSicknessPreventsActivation() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void lethallyDamagedTargetStillDealsItsDamageBack() {
        Permanent creature = addCreatureReady(player1, new GiantSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setMarkedDamage(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof HillGiant);
    }

    @Test
    void abilityCanTargetAnotherCreatureWithTheSameController() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player1, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({FoeRazerRegent.class})
    void mutualDamageDoesNotTriggerFightAbilities() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new FoeRazerRegent());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed({GratuitousViolence.class})
    void eachDamageInstructionUsesItsOwnSourcesDamageModifiers() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PredatoryUrge());
        aura.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(card -> card instanceof GiantSpider);
    }
}
