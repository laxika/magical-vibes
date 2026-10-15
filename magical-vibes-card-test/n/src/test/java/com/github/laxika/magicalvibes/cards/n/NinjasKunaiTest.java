package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NinjasKunai.class, JukaiTrainee.class, TezzeretBetrayerOfFlesh.class})
class NinjasKunaiTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Ninja's Kunai attaches it to a creature")
    void equipsToCreature() {
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kunai.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature sacrifices Ninja's Kunai to deal 3 damage to a player")
    void sacrificesKunaiToDealDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Ninja's Kunai deals 3 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Ninja's Kunai");
    }

    @Test
    void paysCostsBeforeDamageAndResolvesAfterCreatureLeaves() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        kunai.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(creature.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Ninja's Kunai");
        harness.assertInGraveyard(player1, "Ninja's Kunai");
        harness.assertLife(player2, 20);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gameLogContains("damage from Ninja's Kunai")).isTrue();
        assertThat(gameLogContains("damage from Jukai Trainee")).isFalse();
    }

    @Test
    void summoningSickCreatureCannotActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureCannotActivate() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        creature.tap();
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeOpponentsKunai() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player2, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("do not control");

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Ninja's Kunai");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        Permanent creature = addCreatureReady(player2, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(kunai.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damageCannotTargetAnOrdinaryArtifact() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NinjasKunai());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ninja's Kunai");
        assertThat(gd.stack).isEmpty();
    }
    @Test
    void dealsThreeDamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
        harness.assertInGraveyard(player1, "Ninja's Kunai");
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");

        assertThat(kunai.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAbilityCanActivateOutsideMainPhase() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new JukaiTrainee());
        Permanent kunai = harness.addToBattlefieldAndReturn(player1, new NinjasKunai());
        kunai.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Ninja's Kunai");
    }

}
